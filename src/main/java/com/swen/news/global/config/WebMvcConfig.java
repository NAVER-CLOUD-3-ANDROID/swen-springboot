package com.swen.news.global.config;

import com.swen.news.global.resolver.StrictPageableResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC의 Pageable 파라미터 처리를 위한 커스텀 설정 클래스입니다.
 *
 * <p>StrictPageableResolver를 사용하여 페이지 번호 및 크기에 대한 엄격한 유효성 검사를 수행하며, 기본 페이지 및 크기, 최대 페이지 크기를 설정합니다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 커스텀 PageableHandlerMethodArgumentResolver를 등록합니다. 기본 페이지는 0, 기본 사이즈는 10이며, 최대 사이즈는 100으로
     * 제한합니다.
     *
     * @param resolvers 컨트롤러 메서드 인자 리졸버 리스트
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new StrictPageableResolver();

        pageableResolver.setFallbackPageable(PageRequest.of(0, 10)); // 기본값: page=0, size=10
        pageableResolver.setMaxPageSize(100); // size 최대값 제한

        resolvers.add(pageableResolver);
    }
}
