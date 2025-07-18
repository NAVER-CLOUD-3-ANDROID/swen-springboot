package com.swen.news.domain.news.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swen.news.domain.news.code.NewsErrorCode;
import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.embedding.EmbeddingResponse;
import com.swen.news.domain.news.entity.NewsEmbedding;
import com.swen.news.domain.news.exception.NewsException;
import com.swen.news.domain.news.repository.NewsEmbeddingRepository;
import com.swen.news.global.client.HyperClovaEmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 임베딩 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final HyperClovaEmbeddingClient embeddingClient;
    private final NewsEmbeddingRepository newsEmbeddingRepository;
    private final ObjectMapper objectMapper;

    @Value("${external-api.naver.hyperclova.embedding-api-key}")
    private String embeddingApiKey;

    @Value("${external-api.naver.hyperclova.embedding-request-id}")
    private String embeddingRequestId;

    @Override
    public EmbeddingResponse generateEmbedding(String text) {
        try {
            log.info("HyperCLOVA 임베딩 생성 시작 - 텍스트 길이: {}", text.length());

            String requestBody = buildEmbeddingRequest(text);
            String response = embeddingClient.generateEmbedding(
                "Bearer " + embeddingApiKey,
                embeddingRequestId,
                "application/json",
                requestBody
            );

            return parseEmbeddingResponse(response, text);

        } catch (Exception e) {
            log.error("HyperCLOVA 임베딩 생성 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.EMBEDDING_GENERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void saveNewsEmbedding(NewsItem newsItem) {
        try {
            // 이미 저장된 뉴스인지 확인
            if (newsEmbeddingRepository.findByNewsUrl(newsItem.getLink()).isPresent()) {
                log.debug("이미 저장된 뉴스 스킵: {}", newsItem.getLink());
                return;
            }

            // 뉴스 텍스트 전처리
            String preprocessedText = preprocessNewsText(newsItem);
            
            // 임베딩 생성
            EmbeddingResponse embeddingResponse = generateEmbedding(preprocessedText);
            
            // 벡터를 JSON 문자열로 변환
            String vectorJson = objectMapper.writeValueAsString(embeddingResponse.getEmbedding());
            
            // 엔티티 생성 및 저장
            NewsEmbedding newsEmbedding = NewsEmbedding.builder()
                .newsUrl(newsItem.getLink())
                .title(newsItem.getTitle())
                .description(newsItem.getDescription())
                .publisher(newsItem.getPublisher())
                .embeddingVector(vectorJson)
                .vectorDimension(embeddingResponse.getDimension())
                .build();

            newsEmbeddingRepository.save(newsEmbedding);
            log.info("뉴스 임베딩 저장 완료: {}", newsItem.getTitle());

        } catch (Exception e) {
            log.error("뉴스 임베딩 저장 중 오류 발생: {}", newsItem.getTitle(), e);
            // 임베딩 저장 실패가 전체 플로우를 막지 않도록 예외를 던지지 않음
        }
    }

    @Override
    public double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("벡터 차원이 일치하지 않습니다");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    public String preprocessNewsText(NewsItem newsItem) {
        StringBuilder text = new StringBuilder();
        
        // 제목 추가 (가중치를 위해 2번 추가)
        text.append(newsItem.getTitle()).append(" ");
        text.append(newsItem.getTitle()).append(" ");
        
        // 설명 추가
        text.append(newsItem.getDescription());
        
        // HTML 태그 제거 및 특수문자 정리
        String cleanText = text.toString()
            .replaceAll("<[^>]*>", "")  // HTML 태그 제거
            .replaceAll("&[^;]+;", "")  // HTML 엔티티 제거
            .replaceAll("[\\r\\n\\t]+", " ")  // 개행/탭을 공백으로
            .replaceAll("\\s+", " ")  // 연속 공백 제거
            .trim();

        // 최대 길이 제한 (임베딩 API 제한에 따라)
        if (cleanText.length() > 2000) {
            cleanText = cleanText.substring(0, 2000);
        }

        return cleanText;
    }

    /**
     * HyperCLOVA Embedding API 요청 본문 생성
     */
    private String buildEmbeddingRequest(String text) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            objectMapper.createObjectNode()
                .put("text", text)
        );
    }

    /**
     * HyperCLOVA Embedding API 응답 파싱
     */
    private EmbeddingResponse parseEmbeddingResponse(String response, String originalText) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            
            // 응답 구조에 따라 파싱 (실제 API 응답 구조에 맞게 수정 필요)
            JsonNode resultNode = rootNode.get("result");
            if (resultNode != null) {
                JsonNode embeddingNode = resultNode.get("embedding");
                if (embeddingNode != null && embeddingNode.isArray()) {
                    List<Double> embedding = objectMapper.convertValue(
                        embeddingNode, new TypeReference<List<Double>>() {}
                    );
                    
                    return EmbeddingResponse.builder()
                        .embedding(embedding)
                        .text(originalText)
                        .dimension(embedding.size())
                        .build();
                }
            }
            
            log.error("HyperCLOVA Embedding 응답 파싱 실패: {}", response);
            throw new NewsException(NewsErrorCode.EMBEDDING_GENERATION_FAILED);
            
        } catch (Exception e) {
            log.error("HyperCLOVA Embedding 응답 파싱 중 오류 발생", e);
            throw new NewsException(NewsErrorCode.EMBEDDING_GENERATION_FAILED);
        }
    }
}
