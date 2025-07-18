package com.swen.news.domain.news.service;

import com.swen.news.domain.news.dto.NewsItem;
import com.swen.news.domain.news.dto.embedding.VectorSimilarityDto;

import java.util.List;

/**
 * 뉴스 추천 서비스 인터페이스
 */
public interface NewsRecommendationService {
    
    /**
     * 생성된 스크립트를 기반으로 관련 뉴스 추천
     */
    List<NewsItem> findSimilarNewsByScript(String script, NewsItem currentNews);
    
    /**
     * 뉴스 아이템을 기반으로 관련 뉴스 추천
     */
    List<NewsItem> findSimilarNewsByContent(NewsItem currentNews);
    
    /**
     * 유사도 기반 뉴스 랭킹 및 필터링
     */
    List<VectorSimilarityDto> calculateSimilarityRanking(NewsItem targetNews, List<NewsItem> candidateNews);
}
