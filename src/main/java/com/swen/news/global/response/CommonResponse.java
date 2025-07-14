package com.swen.news.global.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * API 응답 공통 래퍼 클래스입니다.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonResponse<T> {
    private boolean isSuccess; // 요청 성공 여부
    private int status; // HTTP 상태코드
    private T data; // 응답 데이터
    private LocalDateTime timestamp; // 응답 생성 시간

    /**
     * 성공 응답 생성 팩토리 메서드
     *
     * @param status HTTP 상태 코드
     * @param data 성공 시 반환할 데이터
     * @param <T> 데이터 타입
     * @return 성공을 나타내는 CommonResponse 객체
     */
    public static <T> CommonResponse<T> onSuccess(int status, T data) {
        return CommonResponse.<T>builder()
                .isSuccess(true)
                .status(status)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 실패 응답 생성 팩토리 메서드
     *
     * @param status HTTP 상태 코드
     * @param data 실패 시 반환할 오류 정보 등
     * @param <T> 데이터 타입
     * @return 실패를 나타내는 CommonResponse 객체
     */
    public static <T> CommonResponse<T> onFailure(int status, T data) {
        return CommonResponse.<T>builder()
                .isSuccess(false)
                .status(status)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
