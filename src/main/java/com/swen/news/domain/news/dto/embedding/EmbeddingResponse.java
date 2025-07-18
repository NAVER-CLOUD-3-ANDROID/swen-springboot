package com.swen.news.domain.news.dto.embedding;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * HyperCLOVA Embedding API 응답 DTO
 */
@Getter
@Builder
public class EmbeddingResponse {
    private final List<Double> embedding;
    private final String text;
    private final int dimension;
}
