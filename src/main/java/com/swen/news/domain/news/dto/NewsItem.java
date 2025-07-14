package com.swen.news.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 네이버 뉴스 API 응답 항목을 나타내는 DTO 클래스입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsItem {
    
    /**
     * 뉴스 제목
     */
    private String title;
    
    /**
     * 뉴스 원문 링크
     */
    private String originallink;
    
    /**
     * 네이버 뉴스 링크
     */
    private String link;
    
    /**
     * 뉴스 요약 내용
     */
    private String description;
    
    /**
     * 언론사명
     */
    private String publisher;
    
    /**
     * 발행일자
     */
    private LocalDateTime pubDate;
}
