package com.swen.news.domain.news.scheduler;

import com.swen.news.domain.news.batch.NewsEmbeddingBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 뉴스 임베딩 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.news-embedding.enabled", havingValue = "true", matchIfMissing = true)
public class NewsEmbeddingScheduler {

    private final NewsEmbeddingBatchService newsEmbeddingBatchService;

    /**
     * 매일 새벽 2시에 최신 뉴스 임베딩 생성
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void collectLatestNewsDaily() {
        log.info("일일 뉴스 임베딩 스케줄 작업 시작");
        newsEmbeddingBatchService.collectAndEmbedLatestNews();
    }

    /**
     * 매 6시간마다 뉴스 임베딩 업데이트 (주간 시간대만)
     */
    @Scheduled(cron = "0 0 6,12,18 * * *")
    public void collectNewsEvery6Hours() {
        log.info("6시간 주기 뉴스 임베딩 스케줄 작업 시작");
        newsEmbeddingBatchService.collectAndEmbedLatestNews();
    }

    /**
     * 애플리케이션 시작 5분 후 초기 뉴스 임베딩 생성 (개발용)
     */
    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 300000) // 5분 후 1회만 실행
    @ConditionalOnProperty(name = "scheduler.news-embedding.initial-run", havingValue = "true", matchIfMissing = false)
    public void initialNewsEmbedding() {
        log.info("초기 뉴스 임베딩 스케줄 작업 시작");
        newsEmbeddingBatchService.collectAndEmbedLatestNews();
    }
}
