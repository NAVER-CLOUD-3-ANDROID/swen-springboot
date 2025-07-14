package com.swen.news.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 뉴스 플레이 요청을 위한 DTO 클래스입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayNewsRequest {
    
    /**
     * 뉴스 주제 또는 키워드 (선택사항)
     * 입력하지 않으면 랜덤 최신 뉴스를 제공
     */
    private String topic;
    
    /**
     * 스크립트 길이 (1분 내 기본값)
     * SHORT: 1분 내, MEDIUM: 3분, LONG: 5분
     */
    @Builder.Default
    private String scriptLength = "SHORT";
    
    // voiceStyle은 제거 - 서비스에서 고정값 사용
}
