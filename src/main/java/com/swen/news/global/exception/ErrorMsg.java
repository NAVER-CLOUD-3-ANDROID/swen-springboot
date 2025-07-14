package com.swen.news.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 에러 메시지를 표현하는 클래스입니다.
 *
 * <p>에러 코드와 사용자에게 전달할 이유(reason)를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMsg {
    private String code; // 에러 코드
    private String reason; // 에러 사유
}