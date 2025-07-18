package com.swen.news.domain.news.service;

import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.embedding.EmbeddingResponse;

import java.util.List;

/**
 * 임베딩 서비스 인터페이스
 */
public interface EmbeddingService {
    
    /**
     * 텍스트를 벡터로 변환
     */
    EmbeddingResponse generateEmbedding(String text);
    
    /**
     * 뉴스 아이템을 임베딩으로 변환하여 저장
     */
    void saveNewsEmbedding(NewsItem newsItem);
    
    /**
     * 두 벡터 간의 코사인 유사도 계산
     */
    double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2);
    
    /**
     * 뉴스 텍스트를 임베딩용 텍스트로 전처리
     */
    String preprocessNewsText(NewsItem newsItem);
}
