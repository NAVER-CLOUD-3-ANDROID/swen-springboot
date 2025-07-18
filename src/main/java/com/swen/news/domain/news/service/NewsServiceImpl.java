package com.swen.news.domain.news.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swen.news.domain.news.code.NewsErrorCode;
import com.swen.news.domain.news.dto.*;
import com.swen.news.domain.news.exception.NewsException;
import com.swen.news.global.client.ClovaVoiceClient;
import com.swen.news.global.client.HyperClovaClient;
import com.swen.news.global.client.NaverNewsClient;
import com.swen.news.global.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 뉴스 서비스 구현 클래스입니다.
 * 글로벌 응답/예외 처리 시스템을 사용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NaverNewsClient naverNewsClient;
    private final HyperClovaClient hyperClovaClient;
    private final ClovaVoiceClient clovaVoiceClient;
    private final ObjectStorageService objectStorageService;
    private final EmbeddingService embeddingService;
    private final NewsRecommendationService newsRecommendationService;
    private final ObjectMapper objectMapper;

    // 고정 음성 설정 - 신뢰가는 차분한 톤 (수진)
    private static final String TTS_SPEAKER = "nsujin";

    @Value("${external-api.naver.news.client-id}")
    private String naverClientId;

    @Value("${external-api.naver.news.client-secret}")
    private String naverClientSecret;

    @Value("${external-api.naver.hyperclova.api-key}")
    private String hyperClovaApiKey;

    @Value("${external-api.naver.hyperclova.request-id}")
    private String hyperClovaRequestId;

    @Value("${external-api.naver.clova-voice.client-id}")
    private String clovaVoiceClientId;

    @Value("${external-api.naver.clova-voice.client-secret}")
    private String clovaVoiceClientSecret;

    @Override
    public NewsScriptResponse playNews(PlayNewsRequest request) {
        log.info("뉴스 플레이 시작 - 주제: {}, 스크립트 길이: {}", request.getTopic(), request.getScriptLength());

        try {
            // 1. 검색어 결정
            String searchQuery = determineSearchQuery(request.getTopic());

            // 2. 뉴스 검색 (1개만)
            NewsSearchRequest searchRequest = NewsSearchRequest.builder()
                    .query(searchQuery)
                    .display(1)
                    .sort("date")
                    .build();

            String newsJson = searchNews(searchRequest);
            List<NewsItem> newsItems = parseNewsItems(newsJson);

            if (newsItems.isEmpty()) {
                throw new NewsException(NewsErrorCode.NEWS_SEARCH_FAILED);
            }

            // 3. 스크립트 생성
            String script = generateScript(newsItems, request.getScriptLength());

            // 4. TTS 변환
            String audioUrl = generateSpeech(script);

            // 5. 현재 뉴스를 임베딩 DB에 저장 (비동기적으로)
            try {
                embeddingService.saveNewsEmbedding(newsItems.get(0));
            } catch (Exception e) {
                log.warn("뉴스 임베딩 저장 실패, 서비스 계속 진행: {}", e.getMessage());
            }

            // 6. 관련 뉴스 추천
            List<NewsItem> recommendedNews = newsRecommendationService.findSimilarNewsByScript(script, newsItems.get(0));

            // 🚀 7. 추천된 뉴스들도 벡터DB에 저장 (학습 효과)
            if (!recommendedNews.isEmpty()) {
                try {
                    for (NewsItem recommended : recommendedNews) {
                        embeddingService.saveNewsEmbedding(recommended);
                    }
                    log.info("추천 뉴스 {}건 벡터DB 저장 요청 완료", recommendedNews.size());
                } catch (Exception e) {
                    log.warn("추천 뉴스 벡터DB 저장 실패: {}", e.getMessage());
                }
            }

            // 8. 응답 생성
            return NewsScriptResponse.builder()
                    .scriptId(UUID.randomUUID().toString())
                    .script(script)
                    .sourceNews(newsItems)
                    .audioUrl(audioUrl)
                    .recommendedNews(recommendedNews)
                    .status("COMPLETED")
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

        } catch (NewsException e) {
            throw e; // 이미 NewsException인 경우 그대로 재던짐
        } catch (Exception e) {
            log.error("뉴스 플레이 처리 중 예상치 못한 오류 발생", e);
            throw new NewsException(NewsErrorCode.EXTERNAL_API_UNAVAILABLE);
        }
    }

    @Override
    public String searchNews(NewsSearchRequest request) {
        try {
            // 검색어 결정 - query가 없으면 랜덤 키워드 사용
            String finalQuery = determineSearchQuery(request.getQuery());
            log.info("네이버 뉴스 검색 시작 - 검색어: {} (원본: {})", finalQuery, request.getQuery());

            return naverNewsClient.searchNews(
                    naverClientId,
                    naverClientSecret,
                    finalQuery,  // 결정된 검색어 사용
                    request.getDisplay(),
                    request.getStart(),
                    request.getSort()
            );
        } catch (Exception e) {
            log.error("네이버 뉴스 검색 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.NEWS_SEARCH_FAILED);
        }
    }

    @Override
    public String generateScript(List<NewsItem> newsItems, String scriptLength) {
        try {
            log.info("CLOVA Studio 스크립트 생성 시작 - 뉴스 개수: {}, 길이: {}", newsItems.size(), scriptLength);

            String prompt = buildScriptPrompt(newsItems, scriptLength);
            String requestBody = buildHyperClovaRequest(prompt);

            String response = hyperClovaClient.generateScript(
                    "Bearer " + hyperClovaApiKey,  // Authorization: Bearer 형식
                    hyperClovaRequestId,
                    "application/json",             // Content-Type
                    requestBody
            );

            return extractScriptFromResponse(response);

        } catch (Exception e) {
            log.error("CLOVA Studio 스크립트 생성 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.SCRIPT_GENERATION_FAILED);
        }
    }

    @Override
    public String generateSpeech(String script) {
        try {
            log.info("CLOVA Voice TTS 변환 시작");

            String requestBody = buildTtsRequest(script);

            byte[] audioData = clovaVoiceClient.generateSpeech(
                    clovaVoiceClientId,
                    clovaVoiceClientSecret,
                    "application/x-www-form-urlencoded",
                    requestBody
            );

            log.info("TTS 변환 완료 - 음성 데이터 크기: {} bytes", audioData.length);

            // Object Storage에 업로드하고 URL 반환
            String audioUrl = objectStorageService.uploadAudioFile(audioData);

            log.info("음성 파일 업로드 완료 - URL: {}", audioUrl);
            return audioUrl;

        } catch (Exception e) {
            log.error("CLOVA Voice TTS 변환 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.TTS_CONVERSION_FAILED);
        }
    }

    /**
     * 검색어 결정 - 주제가 없으면 랜덤 키워드 사용
     */
    private String determineSearchQuery(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            // 랜덤 최신 뉴스용 인기 키워드들
            String[] randomKeywords = {
                    "최신", "오늘", "속보", "정부", "경제", "기술", "사회", "문화", "스포츠", "날씨",
                    "정치", "국제", "증시", "부동산", "교육", "과학", "환경", "건강", "여행", "음식"
            };

            int randomIndex = (int) (Math.random() * randomKeywords.length);
            String selectedKeyword = randomKeywords[randomIndex];
            log.info("랜덤 키워드 선택: {}", selectedKeyword);
            return selectedKeyword;
        }
        return topic.trim();
    }

    /**
     * 뉴스 JSON 데이터를 NewsItem 리스트로 파싱
     */
    private List<NewsItem> parseNewsItems(String newsJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(newsJson);
            JsonNode itemsNode = rootNode.get("items");

            List<NewsItem> newsItems = new ArrayList<>();
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    NewsItem newsItem = NewsItem.builder()
                            .title(cleanHtmlTags(itemNode.get("title").asText()))
                            .link(itemNode.get("link").asText())
                            .originallink(itemNode.get("originallink") != null ? itemNode.get("originallink").asText() : "")
                            .description(cleanHtmlTags(itemNode.get("description").asText()))
                            .publisher(itemNode.get("publisher") != null ? itemNode.get("publisher").asText() : "")
                            .pubDate(LocalDateTime.now()) // 실제로는 pubDate 파싱 필요
                            .build();
                    newsItems.add(newsItem);
                }
            }
            return newsItems;
        } catch (Exception e) {
            log.error("뉴스 아이템 파싱 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * CLOVA Studio용 프롬프트 생성
     */
    private String buildScriptPrompt(List<NewsItem> newsItems, String scriptLength) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 뉴스 기사를 바탕으로 ");

        switch (scriptLength) {
            case "SHORT" -> prompt.append("30초 내로 읽을 수 있는 간단한 ");
            case "LONG" -> prompt.append("3분 분량의 상세한 ");
            default -> prompt.append("1분 분량의 ");
        }

        prompt.append("뉴스 리포트 스크립트를 작성해주세요.\n\n");
        prompt.append("스타일: 신뢰가는 차분한 톤\n");
        prompt.append("요구사항:\n");
        prompt.append("- 자연스러운 한국어로 작성\n");
        prompt.append("- 반드시 '안녕하십니까, [카테고리]에 관련된 내용입니다.'로 시작할 것\n");
        prompt.append("- 기승전결이 담기도록 작성\n");
        prompt.append("- 뉴스 내용만 포함하고, 제목이나 언론사명은 언급하지 말 것\n");
        prompt.append("- [시작], [끝] 같은 표시 문구는 사용하지 말 것\n");
        prompt.append("- 중요한 정보를 명확하게 전달\n");
        prompt.append("- 듣기 좋은 속도와 리듬으로 작성\n");
        prompt.append("- 친근하면서도 정확한 정보 전달\n");
        prompt.append("- 제공된 뉴스 요약 정보와 link만 활용\n");
        prompt.append("- 스크립트의 맨 마지막은 반드시 '이상입니다.'로 끝낼 것\n\n");

        prompt.append("뉴스 정보:\n");
        for (int i = 0; i < newsItems.size(); i++) {
            NewsItem item = newsItems.get(i);
            prompt.append(String.format("%d. 내용: %s\n", i + 1, item.getDescription()));
            prompt.append("\n");
        }

        prompt.append("주의사항:\n");
        prompt.append("- 위 뉴스 내용만을 바탕으로 스크립트를 작성하세요\n");
        prompt.append("- 추가적인 정보를 추측하거나 생성하지 마세요\n");
        prompt.append("- 제목, 언론사명, 링크 정보는 스크립트에 포함하지 마세요\n");
        prompt.append("- 반드시 '안녕하십니까, [적절한 카테고리]에 관련된 내용입니다.'로 시작하세요\n");
        prompt.append("- 스크립트는 반드시 '이상입니다.'로 마무리해주세요");

        return prompt.toString();
    }

    /**
     * CLOVA Studio 문장생성 API 요청 본문 생성
     */
    private String buildHyperClovaRequest(String prompt) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("maxTokens", 500)  // CLOVA Studio는 maxTokens 사용
                        .put("temperature", 0.7)
                        .put("topP", 0.9)  // CLOVA Studio는 topP 사용
                        .put("includeAiFilters", true)
                        .put("seed", 0)
                        .set("messages", objectMapper.createArrayNode()
                                .add(objectMapper.createObjectNode()
                                        .put("role", "system")
                                        .put("content", "당신은 뉴스 스크립트를 작성하는 전문가입니다.")
                                )
                                .add(objectMapper.createObjectNode()
                                        .put("role", "user")
                                        .put("content", prompt)
                                )
                        )
        );
    }

    /**
     * CLOVA Voice TTS 요청 본문 생성 (form-encoded)
     */
    private String buildTtsRequest(String script) {
        try {
            // URL 인코딩된 form 데이터 형태로 구성
            String encodedText = java.net.URLEncoder.encode(script, "UTF-8");
            return String.format("speaker=%s&text=%s&volume=0&speed=-1&pitch=0&format=mp3",
                    TTS_SPEAKER, encodedText);
        } catch (Exception e) {
            log.error("TTS 요청 본문 생성 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.TTS_CONVERSION_FAILED);
        }
    }

    /**
     * CLOVA Studio 응답에서 스크립트 추출
     */
    private String extractScriptFromResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);

            // CLOVA Studio 응답 구조 확인
            JsonNode resultNode = rootNode.get("result");
            if (resultNode != null) {
                JsonNode messageNode = resultNode.get("message");
                if (messageNode != null) {
                    JsonNode contentNode = messageNode.get("content");
                    if (contentNode != null) {
                        return contentNode.asText();
                    }
                }
            }

            // 대안 응답 구조 확인
            JsonNode choicesNode = rootNode.get("choices");
            if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).get("message");
                if (messageNode != null) {
                    return messageNode.get("content").asText();
                }
            }

            log.error("CLOVA Studio 응답에서 콘텐츠를 찾을 수 없습니다: {}", response);
            throw new NewsException(NewsErrorCode.SCRIPT_GENERATION_FAILED);
        } catch (Exception e) {
            log.error("CLOVA Studio 응답 파싱 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.SCRIPT_GENERATION_FAILED);
        }
    }

    /**
     * HTML 태그 제거
     */
    private String cleanHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").replaceAll("&[^;]+;", "");
    }
}
