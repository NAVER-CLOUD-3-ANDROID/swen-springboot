package com.swen.news.domain.news.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.NewsSearchRequest;
import com.swen.news.global.client.NaverNewsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 벡터DB가 비어있을 때 사용하는 Fallback 추천 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackRecommendationService {

    private final NaverNewsClient naverNewsClient;
    private final ObjectMapper objectMapper;
    private final EmbeddingService embeddingService;

    @Value("${external-api.naver.news.client-id}")
    private String naverClientId;

    @Value("${external-api.naver.news.client-secret}")
    private String naverClientSecret;

    /**
     * 간단한 키워드 기반 Fallback 추천
     */
    public List<NewsItem> getFallbackRecommendations(NewsItem currentNews, int maxCount) {
        try {
            log.info("Fallback 추천 시작: {}", currentNews.getTitle());

            // 1. 현재 뉴스에서 키워드 추출
            List<String> keywords = extractSimpleKeywords(currentNews);
            List<NewsItem> recommendations = new ArrayList<>();

            // 2. 키워드로 뉴스 검색
            for (String keyword : keywords) {
                List<NewsItem> searchResults = searchByKeyword(keyword, 3);
                
                // 중복 제거하면서 추가
                searchResults.stream()
                    .filter(news -> !news.getLink().equals(currentNews.getLink()))
                    .filter(news -> recommendations.stream()
                        .noneMatch(existing -> existing.getLink().equals(news.getLink())))
                    .forEach(recommendations::add);

                if (recommendations.size() >= maxCount) {
                    break;
                }
            }

            // 3. 부족하면 일반 최신 뉴스로 보완
            if (recommendations.size() < 3) {
                List<NewsItem> latestNews = searchByKeyword("최신", 5);
                latestNews.stream()
                    .filter(news -> !news.getLink().equals(currentNews.getLink()))
                    .filter(news -> recommendations.stream()
                        .noneMatch(existing -> existing.getLink().equals(news.getLink())))
                    .limit(maxCount - recommendations.size())
                    .forEach(recommendations::add);
            }

            log.info("Fallback 추천 완료: {}건", recommendations.size());
            
            // 🚀 중요: 폴백으로 찾은 뉴스들도 벡터DB에 저장 (비동기)
            saveRecommendationsToVectorDB(recommendations);
            
            return recommendations.stream().limit(maxCount).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Fallback 추천 실패", e);
            return getDefaultRecommendations(maxCount);
        }
    }

    /**
     * 간단한 키워드 추출
     */
    private List<String> extractSimpleKeywords(NewsItem news) {
        // 제목에서 의미있는 단어들 추출
        String title = news.getTitle();
        
        // 일반적인 뉴스 키워드들
        List<String> commonKeywords = Arrays.asList(
            "정부", "경제", "정치", "사회", "문화", "스포츠", "기술", "과학", 
            "교육", "환경", "국제", "부동산", "금융", "증시", "AI", "날씨"
        );
        
        List<String> foundKeywords = new ArrayList<>();
        
        // 제목에 포함된 키워드 찾기
        for (String keyword : commonKeywords) {
            if (title.contains(keyword)) {
                foundKeywords.add(keyword);
            }
        }
        
        // 키워드가 없으면 기본 키워드 사용
        if (foundKeywords.isEmpty()) {
            foundKeywords.add("최신");
            foundKeywords.add("뉴스");
        }
        
        return foundKeywords.stream().limit(2).collect(Collectors.toList());
    }

    /**
     * 키워드로 뉴스 검색
     */
    private List<NewsItem> searchByKeyword(String keyword, int count) {
        try {
            String response = naverNewsClient.searchNews(
                naverClientId,
                naverClientSecret,
                keyword,
                count,
                1,
                "date"
            );
            
            return parseNewsItems(response);
            
        } catch (Exception e) {
            log.warn("키워드 '{}' 검색 실패", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 기본 추천 뉴스 (모든 방법이 실패했을 때)
     */
    private List<NewsItem> getDefaultRecommendations(int maxCount) {
        try {
            log.info("기본 추천 뉴스 제공");
            return searchByKeyword("속보", maxCount);
        } catch (Exception e) {
            log.error("기본 추천 뉴스도 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * 뉴스 JSON 파싱
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
                        .originallink(itemNode.get("originallink") != null ? 
                            itemNode.get("originallink").asText() : "")
                        .description(cleanHtmlTags(itemNode.get("description").asText()))
                        .publisher(itemNode.get("publisher") != null ? 
                            itemNode.get("publisher").asText() : "")
                        .pubDate(LocalDateTime.now())
                        .build();
                    newsItems.add(newsItem);
                }
            }
            return newsItems;
        } catch (Exception e) {
            log.error("뉴스 JSON 파싱 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * HTML 태그 제거
     */
    private String cleanHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").replaceAll("&[^;]+;", "");
    }

    /**
     * 폴백으로 찾은 뉴스들을 벡터DB에 저장 (비동기)
     */
    private void saveRecommendationsToVectorDB(List<NewsItem> newsItems) {
        for (NewsItem newsItem : newsItems) {
            try {
                // 비동기로 임베딩 저장 (실패해도 전체 프로세스에 영향 없음)
                embeddingService.saveNewsEmbedding(newsItem);
                log.debug("폴백 뉴스 벡터DB 저장: {}", newsItem.getTitle());
            } catch (Exception e) {
                log.warn("폴백 뉴스 벡터DB 저장 실패: {}", newsItem.getTitle(), e);
                // 저장 실패해도 계속 진행 (사용자 경험에 영향 없음)
            }
        }
        log.info("폴백 뉴스 {}건 벡터DB 저장 요청 완료", newsItems.size());
    }
}
