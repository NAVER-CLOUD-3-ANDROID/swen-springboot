package com.swen.news.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 뉴스 스크립트 생성 응답을 위한 DTO 클래스입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsScriptResponse {
    
    /**
     * 생성된 스크립트 ID
     */
    private String scriptId;
    
    /**
     * 뉴스 요약 스크립트
     */
    private String script;
    
    /**
     * 참조된 뉴스 목록
     */
    private List<NewsItem> sourceNews;
    
    /**
     * 오디오 파일 URL (TTS 완료 후)
     */
    private String audioUrl;
    
    /**
     * 처리 상태 (PROCESSING, COMPLETED, FAILED)
     */
    private String status;
    
    /**
     * 생성 시간
     */
    private String createdAt;
}
