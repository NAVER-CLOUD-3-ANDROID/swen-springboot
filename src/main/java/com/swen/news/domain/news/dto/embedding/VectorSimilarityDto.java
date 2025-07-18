package com.swen.news.domain.news.dto.embedding;

import com.swen.news.domain.news.dto.NewsItem;
import lombok.Builder;
import lombok.Getter;

/**
 * 벡터 유사도 계산 결과 DTO
 */
@Getter
@Builder
public class VectorSimilarityDto {
    private final NewsItem newsItem;
    private final double similarity;
    private final String matchReason;
}
