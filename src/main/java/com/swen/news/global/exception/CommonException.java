package com.swen.news.global.exception;

import com.swen.news.global.exception.errorcode.BaseErrorCode;
import lombok.Getter;

/**
 * 공통 예외 클래스입니다.
 *
 * <p>애플리케이션 내에서 발생하는 도메인별 오류를 표현하며, 에러 코드와 메시지를 포함합니다.
 */
@Getter
public class CommonException extends RuntimeException {

    /** 예외에 해당하는 에러 코드 */
    private BaseErrorCode errorCode;

    /**
     * 에러 코드를 받아 예외를 생성합니다.
     *
     * @param errorCode 발생한 에러 코드
     */
    public CommonException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
