package com.swen.news.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 네이버 Clova Dubbing TTS API 클라이언트입니다.
 */
@FeignClient(
    name = "clova-dubbing-client",
    url = "${external-api.naver.clova-dubbing.base-url}"
)
public interface ClovaDubbingClient {
    
    /**
     * TTS 음성 생성 API 호출
     *
     * @param accessKey NCP 액세스 키 ID
     * @param secretKey NCP 시크릿 키
     * @param timestamp 타임스탬프
     * @param signature 인증 서명
     * @param request 요청 본문
     * @return TTS 응답
     */
    @PostMapping("/tts-premium/v1/tts")
    String generateSpeech(
        @RequestHeader("X-NCP-APIGW-API-KEY-ID") String accessKey,
        @RequestHeader("X-NCP-APIGW-API-KEY") String secretKey,
        @RequestHeader("X-NCP-APIGW-TIMESTAMP") String timestamp,
        @RequestHeader("X-NCP-APIGW-SIGNATURE-V2") String signature,
        @RequestBody String request
    );
}
