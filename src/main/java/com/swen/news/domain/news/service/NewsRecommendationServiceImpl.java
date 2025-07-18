package com.swen.news.domain.news.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.embedding.EmbeddingResponse;
import com.swen.news.domain.news.dto.embedding.VectorSimilarityDto;
import com.swen.news.domain.news.entity.NewsEmbedding;
import com.swen.news.domain.news.repository.NewsEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 뉴스 추천 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRecommendationServiceImpl implements NewsRecommendationService {

    private final EmbeddingService embeddingService;
    private final NewsEmbeddingRepository newsEmbeddingRepository;
    private final ObjectMapper objectMapper;

    @Value("${vector-db.similarity-threshold:0.7}")
    private double similarityThreshold;

    @Value("${vector-db.max-recommendations:5}")
    private int maxRecommendations;

    @Override
    public List<NewsItem> findSimilarNewsByScript(String script, NewsItem currentNews) {
        try {
            log.info("스크립트 기반 관련 뉴스 검색 시작");

            // 1. 스크립트를 임베딩으로 변환
            EmbeddingResponse scriptEmbedding = embeddingService.generateEmbedding(script);

            // 2. 최근 뉴스 임베딩들 조회 (최근 7일)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<NewsEmbedding> recentEmbeddings = newsEmbeddingRepository.findRecentEmbeddings(weekAgo);

            // 3. 현재 뉴스 제외
            List<NewsEmbedding> candidateEmbeddings = recentEmbeddings.stream()
                .filter(embedding -> !embedding.getNewsUrl().equals(currentNews.getLink()))
                .collect(Collectors.toList());

            // 4. 유사도 계산 및 정렬
            List<VectorSimilarityDto> similarities = calculateSimilarities(
                scriptEmbedding.getEmbedding(), candidateEmbeddings
            );

            // 5. 임계값 이상의 유사도를 가진 뉴스들만 필터링
            return similarities.stream()
                .filter(sim -> sim.getSimilarity() >= similarityThreshold)
                .limit(maxRecommendations)
                .map(VectorSimilarityDto::getNewsItem)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("스크립트 기반 뉴스 추천 중 오류 발생", e);
            // 추천 실패 시 빈 리스트 반환 (전체 서비스 영향 최소화)
            return new ArrayList<>();
        }
    }

    @Override
    public List<NewsItem> findSimilarNewsByContent(NewsItem currentNews) {
        try {
            log.info("콘텐츠 기반 관련 뉴스 검색 시작: {}", currentNews.getTitle());

            // 1. 현재 뉴스를 임베딩으로 변환
            String preprocessedText = embeddingService.preprocessNewsText(currentNews);
            EmbeddingResponse currentEmbedding = embeddingService.generateEmbedding(preprocessedText);

            // 2. 현재 뉴스를 제외한 모든 뉴스 임베딩 조회
            List<NewsEmbedding> candidateEmbeddings = newsEmbeddingRepository.findAllExcludingUrl(currentNews.getLink());

            // 3. 유사도 계산 및 정렬
            List<VectorSimilarityDto> similarities = calculateSimilarities(
                currentEmbedding.getEmbedding(), candidateEmbeddings
            );

            // 4. 상위 추천 뉴스 반환
            return similarities.stream()
                .filter(sim -> sim.getSimilarity() >= similarityThreshold)
                .limit(maxRecommendations)
                .map(VectorSimilarityDto::getNewsItem)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("콘텐츠 기반 뉴스 추천 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<VectorSimilarityDto> calculateSimilarityRanking(NewsItem targetNews, List<NewsItem> candidateNews) {
        try {
            // 타겟 뉴스 임베딩 생성
            String preprocessedText = embeddingService.preprocessNewsText(targetNews);
            EmbeddingResponse targetEmbedding = embeddingService.generateEmbedding(preprocessedText);

            List<VectorSimilarityDto> similarities = new ArrayList<>();

            for (NewsItem candidate : candidateNews) {
                try {
                    String candidateText = embeddingService.preprocessNewsText(candidate);
                    EmbeddingResponse candidateEmbedding = embeddingService.generateEmbedding(candidateText);

                    double similarity = embeddingService.calculateCosineSimilarity(
                        targetEmbedding.getEmbedding(),
                        candidateEmbedding.getEmbedding()
                    );

                    String matchReason = generateMatchReason(targetNews, candidate, similarity);

                    similarities.add(VectorSimilarityDto.builder()
                        .newsItem(candidate)
                        .similarity(similarity)
                        .matchReason(matchReason)
                        .build());

                } catch (Exception e) {
                    log.warn("개별 뉴스 유사도 계산 실패: {}", candidate.getTitle());
                }
            }

            // 유사도 기준 내림차순 정렬
            return similarities.stream()
                .sorted(Comparator.comparingDouble(VectorSimilarityDto::getSimilarity).reversed())
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("유사도 랭킹 계산 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 뉴스 임베딩들과 주어진 벡터 간의 유사도 계산
     */
    private List<VectorSimilarityDto> calculateSimilarities(List<Double> targetVector, List<NewsEmbedding> embeddings) {
        List<VectorSimilarityDto> similarities = new ArrayList<>();

        for (NewsEmbedding embedding : embeddings) {
            try {
                // JSON 문자열을 벡터로 변환
                List<Double> embeddingVector = objectMapper.readValue(
                    embedding.getEmbeddingVector(), new TypeReference<List<Double>>() {}
                );

                // 유사도 계산
                double similarity = embeddingService.calculateCosineSimilarity(targetVector, embeddingVector);

                // NewsItem 객체 생성
                NewsItem newsItem = NewsItem.builder()
                    .title(embedding.getTitle())
                    .link(embedding.getNewsUrl())
                    .description(embedding.getDescription())
                    .publisher(embedding.getPublisher())
                    .pubDate(embedding.getCreatedAt())
                    .build();

                String matchReason = String.format("유사도: %.2f", similarity);

                similarities.add(VectorSimilarityDto.builder()
                    .newsItem(newsItem)
                    .similarity(similarity)
                    .matchReason(matchReason)
                    .build());

            } catch (Exception e) {
                log.warn("임베딩 벡터 파싱 실패: {}", embedding.getNewsUrl());
            }
        }

        // 유사도 기준 내림차순 정렬
        return similarities.stream()
            .sorted(Comparator.comparingDouble(VectorSimilarityDto::getSimilarity).reversed())
            .collect(Collectors.toList());
    }

    /**
     * 매칭 이유 생성
     */
    private String generateMatchReason(NewsItem target, NewsItem candidate, double similarity) {
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("유사도: %.2f", similarity));

        // 같은 발행사인 경우
        if (target.getPublisher().equals(candidate.getPublisher())) {
            reason.append(" (같은 발행사)");
        }

        // 제목에 공통 키워드가 있는 경우
        String[] targetWords = target.getTitle().split("\\s+");
        String candidateTitle = candidate.getTitle();
        long commonWords = java.util.Arrays.stream(targetWords)
            .filter(word -> word.length() > 1) // 한 글자 단어 제외
            .filter(candidateTitle::contains)
            .count();

        if (commonWords > 0) {
            reason.append(String.format(" (공통키워드: %d개)", commonWords));
        }

        return reason.toString();
    }
}
