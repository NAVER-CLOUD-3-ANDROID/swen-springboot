package com.swen.news.domain.user.exception;

/**
 * User 도메인의 기본 예외 클래스
 * 모든 User 관련 예외들의 부모 클래스
 */
public abstract class UserDomainException extends RuntimeException {
    
    public UserDomainException(String message) {
        super(message);
    }
    
    public UserDomainException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 예외 코드를 반환합니다.
     * 각 구현체에서 오버라이드해서 사용
     */
    public abstract String getErrorCode();
}
