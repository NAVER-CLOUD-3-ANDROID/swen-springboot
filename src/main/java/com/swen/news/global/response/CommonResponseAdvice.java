package com.swen.news.global.response;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 공통 응답 래핑을 처리하는 ResponseBodyAdvice 구현체입니다.
 *
 * <p>컨트롤러에서 반환하는 응답을 가로채어 {@link CommonResponse} 형태로 자동 래핑하여 일관된 API 응답 포맷을 제공합니다.
 *
 * <p>특정 조건에 따라 래핑을 건너뛸 수 있으며, 204 No Content, 이미 CommonResponse 타입, String 응답 등은 래핑하지 않습니다.
 */
@RestControllerAdvice
public class CommonResponseAdvice implements ResponseBodyAdvice {

    /**
     * Advice 적용 가능 여부 결정 메서드.
     *
     * @param returnType 컨트롤러 메서드 반환 타입
     * @param converterType HTTP 메시지 컨버터 타입
     * @return ByteArrayHttpMessageConverter를 제외한 모든 경우에 true 반환 (적용)
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // ByteArray 컨버터라면 Advice 적용 안 함
        if (ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return false;
        }
        return true;
    }

    /**
     * 컨트롤러 응답 바디 작성 전에 호출되어, 응답 바디를 감싸거나 수정할 수 있습니다.
     *
     * @param body 실제 응답 객체
     * @param returnType 메서드 반환 타입 정보
     * @param selectedContentType 선택된 Content-Type
     * @param selectedConverterType 선택된 메시지 컨버터 타입
     * @param request HTTP 요청 정보
     * @param response HTTP 응답 정보
     * @return 래핑된 응답 객체 또는 원본 객체
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // HTTP 상태코드 가져오기
        HttpServletResponse httpServletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();
        int status = httpServletResponse.getStatus();
        HttpStatus httpStatus = HttpStatus.resolve(status);

        // 1) 204 No Content라면, 바디 없이 그대로 두기
        if (httpStatus == HttpStatus.NO_CONTENT) {
            return null;
        }

        // 2) 이미 CommonResponse를 리턴했다면 그대로
        if (body instanceof CommonResponse) {
            return body;
        }

        // 3) 상태 해석 불가 or String인 경우 그대로
        if (httpStatus == null || body instanceof String) {
            return body;
        }

        // 4) 성공(2xx) → 래핑
        if (httpStatus.is2xxSuccessful()) {
            return CommonResponse.onSuccess(status, body);
        }

        // 5) 에러(4xx/5xx) → 래핑
        if (httpStatus.isError()) {
            return CommonResponse.onFailure(status, body);
        }

        return body;
    }
}
