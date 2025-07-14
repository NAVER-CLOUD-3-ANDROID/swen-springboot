package com.swen.news.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 채권 종목 페이징 응답 DTO 클래스입니다.
 *
 * <p>페이징 처리된 결과를 클라이언트에 전달하기 위한 용도로, 현재 페이지의 데이터 목록과 페이징 관련 메타 정보를 포함합니다.
 *
 * @param <T> 페이지 내 각 데이터 항목의 타입
 */
@Schema(description = "채권 종목 페이징 응답")
public class PageResponse<T> {
    @Schema(description = "현재 페이지 데이터 목록")
    private List<T> content;

    @Schema(description = "현재 페이지 번호")
    private int number;

    @Schema(description = "전체 페이지 수")
    private int totalPages;

    @Schema(description = "전체 데이터 수")
    private long totalElements;

    // size, sort 등 필요한 필드 추가 가능
}
