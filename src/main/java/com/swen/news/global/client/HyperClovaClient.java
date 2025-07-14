package com.swen.news.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 네이버 CLOVA Studio API 클라이언트입니다.
 * 문장생성(Chat Completions) API를 사용합니다.
 */
@FeignClient(
    name = "clova-studio-client", 
    url = "${external-api.naver.hyperclova.api-gateway-url}"
)
public interface HyperClovaClient {
    
    /**
     * CLOVA Studio 문장생성 API 호출
     * 
     * @param apiKey X-NCP-CLOVASTUDIO-API-KEY
     * @param apigwApiKey X-NCP-APIGW-API-KEY  
     * @param requestId X-NCP-CLOVASTUDIO-REQUEST-ID
     * @param request 요청 본문
     * @return AI 응답
     */
    @PostMapping
    String generateScript(
        @RequestHeader("X-NCP-CLOVASTUDIO-API-KEY") String apiKey,
        @RequestHeader("X-NCP-APIGW-API-KEY") String apigwApiKey,
        @RequestHeader("X-NCP-CLOVASTUDIO-REQUEST-ID") String requestId,
        @RequestBody String request
    );
}
