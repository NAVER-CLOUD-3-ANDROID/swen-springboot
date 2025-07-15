package com.swen.news.domain.user.exception;

public class UserAlreadyExistsException extends UserDomainException {
    
    private static final String ERROR_CODE = "USER_ALREADY_EXISTS";
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // 이메일 중복을 위한 정적 팩토리 메서드
    public static UserAlreadyExistsException forEmail(String email) {
        return new UserAlreadyExistsException("이미 존재하는 이메일입니다. Email: " + email);
    }
    
    // Provider와 ProviderId 중복을 위한 정적 팩토리 메서드
    public static UserAlreadyExistsException forProvider(String provider, String providerId) {
        return new UserAlreadyExistsException("이미 존재하는 사용자입니다. Provider: " + provider + ", ProviderId: " + providerId);
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
