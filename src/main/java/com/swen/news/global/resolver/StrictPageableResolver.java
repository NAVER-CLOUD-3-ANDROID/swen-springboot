package com.swen.news.global.resolver;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Pageable 파라미터에 대해 엄격한 유효성 검사를 수행하는 커스텀 HandlerMethodArgumentResolver 입니다.
 *
 * <p>페이지 번호와 크기에 대해 음수나 잘못된 형식이 들어올 경우 IllegalArgumentException을 던져 클라이언트에 명확한 오류를 전달합니다.
 */
public class StrictPageableResolver extends PageableHandlerMethodArgumentResolver {

    /**
     * 요청 파라미터에서 페이지 번호와 페이지 크기를 검증합니다. - 페이지 번호는 0 이상이어야 하며, 정수 형식이어야 합니다. - 페이지 크기는 1 이상이어야 하며, 정수
     * 형식이어야 합니다.
     *
     * @param methodParameter 컨트롤러 메서드 파라미터 정보
     * @param mavContainer 모델 및 뷰 컨테이너
     * @param webRequest 웹 요청 정보
     * @param binderFactory 데이터 바인딩 팩토리
     * @return 유효성 검사를 통과한 Pageable 객체
     * @throws IllegalArgumentException 페이지 번호 또는 크기가 유효하지 않은 경우 발생
     */
    @Override
    public Pageable resolveArgument(
            org.springframework.core.MethodParameter methodParameter,
            org.springframework.web.method.support.ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {

        String pageParam = webRequest.getParameter(getPageParameterName());
        String sizeParam = webRequest.getParameter(getSizeParameterName());

        if (pageParam != null) {
            try {
                int page = Integer.parseInt(pageParam);
                if (page < 0) {
                    throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("유효하지 않은 페이지 번호 형식입니다.");
            }
        }

        if (sizeParam != null) {
            try {
                int size = Integer.parseInt(sizeParam);
                if (size < 1) {
                    throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("유효하지 않은 페이지 크기 형식입니다.");
            }
        }

        return super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
    }
}

