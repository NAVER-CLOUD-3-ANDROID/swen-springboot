package com.swen.news.domain.user.exception;

public class InvalidUserDataException extends UserDomainException {
    
    private static final String ERROR_CODE = "INVALID_USER_DATA";
    
    public InvalidUserDataException(String message) {
        super(message);
    }
    
    public InvalidUserDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
