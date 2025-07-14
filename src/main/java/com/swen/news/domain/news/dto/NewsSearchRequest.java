package com.swen.news.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 뉴스 검색 요청을 위한 DTO 클래스입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchRequest {
    
    /**
     * 검색어 (선택사항 - 없으면 랜덤 키워드 사용)
     */
    private String query;
    
    /**
     * 검색 결과 개수 (기본값: 10, 최대: 100)
     */
    @Min(value = 1, message = "검색 결과 개수는 1개 이상이어야 합니다.")
    @Max(value = 100, message = "검색 결과 개수는 100개 이하여야 합니다.")
    @Builder.Default
    private Integer display = 10;
    
    /**
     * 검색 시작 위치 (기본값: 1)
     */
    @Min(value = 1, message = "검색 시작 위치는 1 이상이어야 합니다.")
    @Builder.Default
    private Integer start = 1;
    
    /**
     * 정렬 옵션 (sim: 정확도순, date: 날짜순)
     */
    @Builder.Default
    private String sort = "date";
}
