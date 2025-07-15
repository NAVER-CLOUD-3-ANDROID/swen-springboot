package com.swen.news.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * NCP Object Storage 설정 클래스
 * NCP Object Storage는 AWS S3 호환 API를 제공합니다.
 */
@Slf4j
@Configuration
public class ObjectStorageConfig {
    
    @Value("${ncp.object-storage.endpoint}")
    private String endpoint;
    
    @Value("${ncp.object-storage.access-key}")
    private String accessKey;
    
    @Value("${ncp.object-storage.secret-key}")
    private String secretKey;
    
    @Value("${ncp.region}")
    private String region;
    
    @Bean
    public S3Client s3Client() {
        try {
            log.info("NCP Object Storage S3Client 설정 - endpoint: {}, region: {}", endpoint, region);
            
            return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("kr-standard")) // NCP Object Storage용 region
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true) // NCP Object Storage는 path-style 사용
                .build();
                
        } catch (Exception e) {
            log.error("S3Client 생성 중 오류 발생", e);
            throw new RuntimeException("Object Storage 설정 실패", e);
        }
    }
}
