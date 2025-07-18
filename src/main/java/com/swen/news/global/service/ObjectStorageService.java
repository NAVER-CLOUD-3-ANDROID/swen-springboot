package com.swen.news.global.service;

import com.swen.news.domain.news.code.NewsErrorCode;
import com.swen.news.domain.news.exception.NewsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * NCP Object Storage 서비스 클래스
 * 음성 파일 업로드/삭제 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectStorageService {
    
    private final S3Client s3Client;
    
    @Value("${ncp.object-storage.bucket-name}")
    private String bucketName;
    
    @Value("${ncp.object-storage.endpoint}")
    private String endpoint;
    
    /**
     * 음성 파일을 Object Storage에 업로드하고 공개 URL을 반환합니다.
     *
     * @param audioData 음성 파일 바이너리 데이터
     * @return 업로드된 파일의 공개 URL
     */
    public String uploadAudioFile(byte[] audioData) {
        try {
            // 고유 파일명 생성 (타임스탬프 + UUID)
            String fileName = generateUniqueFileName();
            String objectKey = "audio/" + fileName;
            
            log.info("Object Storage 음성 파일 업로드 시작 - 파일명: {}, 크기: {} bytes", fileName, audioData.length);
            
            // 파일 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("audio/mpeg")
                .contentLength((long) audioData.length)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
            
            s3Client.putObject(putRequest, RequestBody.fromBytes(audioData));
            
            // 공개 URL 생성 (NCP Object Storage 표준 URL 형식)
            String publicUrl = String.format("%s/%s/%s", endpoint, bucketName, objectKey);
            
            log.info("Object Storage 음성 파일 업로드 완료 - URL: {}", publicUrl);
            return publicUrl;
            
        } catch (Exception e) {
            log.error("Object Storage 음성 파일 업로드 실패", e);
            throw new NewsException(NewsErrorCode.FILE_UPLOAD_FAILED);
        }
    }
    
    /**
     * 음성 파일을 Object Storage에서 삭제합니다.
     *
     * @param audioUrl 삭제할 파일의 URL
     */
    public void deleteAudioFile(String audioUrl) {
        try {
            // URL에서 object key 추출
            String objectKey = extractObjectKeyFromUrl(audioUrl);
            if (objectKey == null) {
                log.warn("잘못된 URL 형식으로 파일 삭제 실패: {}", audioUrl);
                return;
            }
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
            
            s3Client.deleteObject(deleteRequest);
            log.info("Object Storage 파일 삭제 완료: {}", objectKey);
            
        } catch (Exception e) {
            log.error("Object Storage 파일 삭제 실패 - URL: {}", audioUrl, e);
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
    
    /**
     * 고유한 파일명을 생성합니다.
     *
     * @return 생성된 파일명 (예: 20250715_143022_abc12345.mp3)
     */
    private String generateUniqueFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.mp3", timestamp, uuid);
    }
    
    /**
     * URL에서 Object Storage의 object key를 추출합니다.
     *
     * @param audioUrl 음성 파일 URL
     * @return object key (예: audio/20250715_143022_abc12345.mp3)
     */
    private String extractObjectKeyFromUrl(String audioUrl) {
        try {
            // endpoint/bucketName 부분을 제거하고 object key만 추출
            String prefix = endpoint + "/" + bucketName + "/";
            if (audioUrl.startsWith(prefix)) {
                return audioUrl.substring(prefix.length());
            }
            return null;
        } catch (Exception e) {
            log.error("Object key 추출 실패: {}", audioUrl, e);
            return null;
        }
    }
}
