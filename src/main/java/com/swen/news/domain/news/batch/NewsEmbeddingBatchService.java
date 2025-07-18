package com.swen.news.domain.news.batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.NewsSearchRequest;
import com.swen.news.domain.news.service.EmbeddingService;
import com.swen.news.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 뉴스 임베딩 배치 작업 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsEmbeddingBatchService {

    private final NewsService newsService;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    /**
     * 최신 뉴스를 수집하여 임베딩 생성 (스케줄러용)
     */
    @Async("embeddingTaskExecutor")
    public void collectAndEmbedLatestNews() {
        try {
            log.info("최신 뉴스 임베딩 배치 작업 시작");

            // 인기 키워드들로 뉴스 수집
            String[] keywords = {
                "최신", "정부", "경제", "기술", "사회", "문화", "스포츠", 
                "정치", "국제", "증시", "부동산", "교육", "과학", "환경"
            };

            int totalProcessed = 0;
            int totalSuccess = 0;

            for (String keyword : keywords) {
                try {
                    List<NewsItem> newsItems = collectNewsByKeyword(keyword, 10);
                    
                    for (NewsItem newsItem : newsItems) {
                        try {
                            embeddingService.saveNewsEmbedding(newsItem);
                            totalSuccess++;
                            
                            // API 호출 제한을 위한 딜레이 (스케줄러에서는 더 긴 딜레이)
                            Thread.sleep(2000);
                            
                        } catch (Exception e) {
                            log.warn("뉴스 임베딩 저장 실패: {}", newsItem.getTitle(), e);
                        }
                        totalProcessed++;
                    }
                    
                    log.info("키워드 '{}' 처리 완료: {}건", keyword, newsItems.size());
                    
                } catch (Exception e) {
                    log.error("키워드 '{}' 처리 중 오류 발생", keyword, e);
                }
            }

            log.info("최신 뉴스 임베딩 배치 작업 완료 - 처리: {}건, 성공: {}건", totalProcessed, totalSuccess);

        } catch (Exception e) {
            log.error("뉴스 임베딩 배치 작업 중 전체 오류 발생", e);
        }
    }

    /**
     * 특정 키워드로 뉴스 수집
     */
    private List<NewsItem> collectNewsByKeyword(String keyword, int count) {
        try {
            NewsSearchRequest request = NewsSearchRequest.builder()
                .query(keyword)
                .display(count)
                .start(1)
                .sort("date")
                .build();

            String newsJson = newsService.searchNews(request);
            return parseNewsItems(newsJson);

        } catch (Exception e) {
            log.error("키워드 '{}' 뉴스 수집 실패", keyword, e);
            return new ArrayList<>();
        }
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
                        .pubDate(LocalDateTime.now())
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
     * HTML 태그 제거
     */
    private String cleanHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").replaceAll("&[^;]+;", "");
    }
}
