package com.swen.news.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 네이버 CLOVA Voice TTS API 클라이언트입니다.
 * 문서: https://api.ncloud-docs.com/docs/ai-naver-clovavoice-ttspremium
 */
@FeignClient(
    name = "clova-voice-client",
    url = "${external-api.naver.clova-voice.url}"
)
public interface ClovaVoiceClient {
    
    /**
     * CLOVA Voice TTS API 호출
     *
     * @param clientId X-NCP-APIGW-API-KEY-ID (Client ID)
     * @param clientSecret X-NCP-APIGW-API-KEY (Client Secret)
     * @param contentType application/x-www-form-urlencoded
     * @param requestBody form-encoded 요청 본문
     * @return TTS 응답 (MP3 바이너리 데이터)
     */
    @PostMapping(consumes = "application/x-www-form-urlencoded")
    byte[] generateSpeech(
        @RequestHeader("X-NCP-APIGW-API-KEY-ID") String clientId,
        @RequestHeader("X-NCP-APIGW-API-KEY") String clientSecret,
        @RequestHeader("Content-Type") String contentType,
        @RequestBody String requestBody
    );
}
