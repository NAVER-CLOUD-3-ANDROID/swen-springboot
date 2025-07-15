package com.swen.news.domain.user.exception;

public class UserNotFoundException extends UserDomainException {
    
    private static final String ERROR_CODE = "USER_NOT_FOUND";
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // 사용자 ID로 찾을 수 없을 때
    public static UserNotFoundException forUserId(Long userId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. UserId: " + userId);
    }
    
    // 이메일로 찾을 수 없을 때
    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. Email: " + email);
    }
    
    // Provider와 ProviderId로 찾을 수 없을 때
    public static UserNotFoundException forProvider(String provider, String providerId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. Provider: " + provider + ", ProviderId: " + providerId);
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
