package com.swen.news.global.exception;

import com.swen.news.global.exception.errorcode.BaseErrorCode;
import com.swen.news.global.exception.errorcode.CommonErrorCode;
import com.swen.news.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * 전역 예외 처리기 클래스입니다.
 *
 * <p>스프링 MVC 컨트롤러에서 발생하는 예외를 포괄적으로 처리하여, 일관된 에러 응답을 반환합니다. 주요 처리 예외:
 *
 * <ul>
 *   <li>도메인 커스텀 예외 (CommonException)
 *   <li>검증 실패 예외 (MethodArgumentNotValidException)
 *   <li>Jackson 역직렬화 오류 (HttpMessageNotReadableException)
 *   <li>잘못된 페이징 파라미터 예외 (IllegalArgumentException)
 *   <li>기타 예기치 않은 예외 (Exception)
 * </ul>
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 비즈니스 로직에서 발생하는 커스텀 예외를 처리합니다. 에러 코드에 정의된 HTTP 상태 코드와 메시지를 사용해 응답을 생성합니다.
     *
     * @param e CommonException 예외 객체
     * @return 에러 메시지를 포함한 공통 응답 객체와 HTTP 상태 코드
     */
    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonResponse<ErrorMsg>> handleCommonException(CommonException e) {
        BaseErrorCode errorCode = e.getErrorCode();
        CommonResponse<ErrorMsg> response =
                CommonResponse.onFailure(
                        errorCode.getHttpStatus().value(), errorCode.getErrorMsg());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * Spring Validation(@Valid) 실패 시 발생하는 예외를 처리합니다. 가장 먼저 발생한 필드 에러 메시지를 반환합니다.
     *
     * @param ex MethodArgumentNotValidException 예외 객체
     * @return 에러 메시지를 포함한 공통 응답 객체와 HTTP 400 상태 코드
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<ErrorMsg>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String errorMessage =
                ex.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .orElse("입력값이 유효하지 않습니다.");

        ErrorMsg error = new ErrorMsg(CommonErrorCode.INVALID_INPUT.getCodeName(), errorMessage);
        return ResponseEntity.badRequest()
                .body(CommonResponse.onFailure(HttpStatus.BAD_REQUEST.value(), error));
    }

    /**
     * Jackson 역직렬화 오류 처리 (예: enum 타입 매핑 실패). 특정 enum 타입에 대해 사용자 친화적인 메시지를 반환합니다.
     *
     * @param ex HttpMessageNotReadableException 예외 객체
     * @return 에러 메시지를 포함한 공통 응답 객체와 HTTP 400 상태 코드
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<ErrorMsg>> handleEnumParsingError(
            HttpMessageNotReadableException ex) {

        // 일반적인 역직렬화 오류 처리
        ErrorMsg error =
                new ErrorMsg(CommonErrorCode.INVALID_INPUT.getCodeName(), "요청 형식이 잘못되었습니다.");
        return ResponseEntity.badRequest()
                .body(CommonResponse.onFailure(HttpStatus.BAD_REQUEST.value(), error));
    }

    /**
     * 페이징 관련 파라미터가 잘못된 경우 발생하는 IllegalArgumentException 처리기입니다.
     *
     * @param e IllegalArgumentException 예외 객체
     * @return 에러 메시지를 포함한 공통 응답 객체와 HTTP 400 상태 코드
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<ErrorMsg>> handleIllegalArg(IllegalArgumentException e) {
        ErrorMsg error =
                new ErrorMsg(CommonErrorCode.INVALID_INPUT.getCodeName(), "페이징 요청 형식이 잘못되었습니다.");
        return ResponseEntity.badRequest()
                .body(CommonResponse.onFailure(HttpStatus.BAD_REQUEST.value(), error));
    }

    /**
     * 기타 예상치 못한 예외를 처리하는 fallback 메서드입니다. 서버 내부 오류 상태 코드와 메시지를 반환합니다.
     *
     * @param ex 예외 객체
     * @return 에러 메시지를 포함한 공통 응답 객체와 HTTP 500 상태 코드
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<ErrorMsg>> handleUnexpectedException(Exception ex) {
        log.error("예상치 못한 오류 발생", ex);

        ErrorMsg error = CommonErrorCode.INTERNAL_SERVER_ERROR.getErrorMsg();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.onFailure(HttpStatus.INTERNAL_SERVER_ERROR.value(), error));
    }
}
