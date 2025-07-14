package com.swen.news.global.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드를 정의한 Enum입니다.
 *
 * <p>HTTP 상태 코드와 사용자에게 보여줄 메시지를 포함하며, 애플리케이션 전반에서 공통으로 사용하는 에러 코드들을 관리합니다.
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    /**
     * 에러 코드 이름을 반환합니다.
     *
     * @return 에러 코드의 이름
     */
    @Override
    public String getCodeName() {
        return this.name();
    }
}