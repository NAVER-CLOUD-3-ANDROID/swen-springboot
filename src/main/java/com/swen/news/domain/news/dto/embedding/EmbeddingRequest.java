package com.swen.news.domain.news.dto.embedding;

import lombok.Builder;
import lombok.Getter;

/**
 * HyperCLOVA Embedding API 요청 DTO
 */
@Getter
@Builder
public class EmbeddingRequest {
    private final String text;
}
