package com.swen.news.global.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 클라이언트 설정 클래스입니다.
 */
@Configuration
@EnableFeignClients(basePackages = "com.swen.news.global.client")
public class FeignConfig {
    
    /**
     * Feign 로그 레벨 설정
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
