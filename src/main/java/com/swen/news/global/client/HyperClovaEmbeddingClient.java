package com.swen.news.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 네이버 HyperCLOVA Embedding API 클라이언트입니다.
 * 문서: https://api.ncloud-docs.com/docs/clovastudio-embedding
 */
@FeignClient(
    name = "hyperclova-embedding-client", 
    url = "${external-api.naver.hyperclova.embedding-url}"
)
public interface HyperClovaEmbeddingClient {
    
    /**
     * HyperCLOVA Embedding API 호출
     * 
     * @param authorization Bearer {API Key} 형식
     * @param requestId X-NCP-CLOVASTUDIO-REQUEST-ID
     * @param contentType application/json
     * @param request 요청 본문
     * @return 임베딩 응답
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    String generateEmbedding(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("X-NCP-CLOVASTUDIO-REQUEST-ID") String requestId,
        @RequestHeader("Content-Type") String contentType,
        @RequestBody String request
    );
}
