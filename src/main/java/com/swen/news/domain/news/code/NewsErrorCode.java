package com.swen.news.domain.news.code;

import com.swen.news.global.exception.errorcode.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 뉴스 도메인 에러 코드 정의 클래스입니다.
 */
@Getter
@AllArgsConstructor
public enum NewsErrorCode implements BaseErrorCode {
    
    // 뉴스 검색 관련 에러
    NEWS_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_001", "뉴스 검색에 실패했습니다."),
    NEWS_API_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "NEWS_002", "뉴스 API 호출 시간이 초과되었습니다."),
    NEWS_API_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "NEWS_003", "뉴스 API 호출 한도를 초과했습니다."),
    
    // 스크립트 생성 관련 에러
    SCRIPT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_101", "스크립트 생성에 실패했습니다."),
    HYPERCLOVA_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_102", "HyperCLOVA API 호출 중 오류가 발생했습니다."),
    INVALID_SCRIPT_LENGTH(HttpStatus.BAD_REQUEST, "NEWS_103", "유효하지 않은 스크립트 길이입니다."),
    
    // TTS 변환 관련 에러
    TTS_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_201", "TTS 변환에 실패했습니다."),
    CLOVA_DUBBING_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_202", "Clova Dubbing API 호출 중 오류가 발생했습니다."),
    INVALID_VOICE_STYLE(HttpStatus.BAD_REQUEST, "NEWS_203", "유효하지 않은 음성 스타일입니다."),
    AUDIO_FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_204", "오디오 파일 저장에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_205", "파일 업로드에 실패했습니다."),
    
    // 임베딩 및 추천 관련 에러
    EMBEDDING_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_401", "임베딩 생성에 실패했습니다."),
    VECTOR_SIMILARITY_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_402", "벡터 유사도 계산에 실패했습니다."),
    NEWS_RECOMMENDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_403", "뉴스 추천에 실패했습니다."),
    
    // 일반 에러
    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "NEWS_301", "잘못된 요청 형식입니다."),
    EXTERNAL_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "NEWS_302", "외부 API 서비스를 사용할 수 없습니다.");
    
    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getCodeName() {
        return code;
    }
}
