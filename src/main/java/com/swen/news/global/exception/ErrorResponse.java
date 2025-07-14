package com.swen.news.global.exception;

/**
 * API 에러 응답을 표현하는 DTO입니다.
 *
 * @param errorClassName 예외 클래스 이름
 * @param message 사용자에게 전달할 에러 메시지
 */
public record ErrorResponse(String errorClassName, String message) {
    public static ErrorResponse of(String errorClassName, String message) {
        return new ErrorResponse(errorClassName, message);
    }
}
