package com.swen.news.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "embeddingTaskExecutor")
    public Executor embeddingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Embedding-");
        executor.initialize();
        return executor;
    }
}
