package com.swen.news.domain.user.exception;

public class UserServiceException extends UserDomainException {
    
    private static final String ERROR_CODE = "USER_SERVICE_ERROR";
    
    public UserServiceException(String message) {
        super(message);
    }
    
    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
