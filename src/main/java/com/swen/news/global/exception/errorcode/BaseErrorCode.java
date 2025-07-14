package com.swen.news.global.exception.errorcode;

import com.swen.news.global.exception.ErrorMsg;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용하는 공통 에러 코드 인터페이스입니다.
 *
 * <p>HTTP 상태 코드, 에러 메시지, 에러 코드 이름을 제공하며, 기본적으로 {@link ErrorMsg} 객체를 생성하는 메서드를 포함합니다.
 */
public interface BaseErrorCode {
    HttpStatus getHttpStatus();

    String getMessage();

    String getCodeName();

    /**
     * {@link ErrorMsg} 객체를 생성하여 반환합니다. 기본 구현으로, 코드 이름과 메시지를 포함한 {@code ErrorMsg}를 반환합니다.
     *
     * @return 에러 메시지 객체
     */
    default ErrorMsg getErrorMsg() {
        return ErrorMsg.builder().code(getCodeName()).reason(getMessage()).build();
    }
}
