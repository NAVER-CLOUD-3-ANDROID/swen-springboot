package com.swen.news.domain.user.validator;

import com.swen.news.domain.user.exception.InvalidUserDataException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class UserValidator {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern NICKNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9가-힣]{2,20}$");
    
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 50;
    
    public static void validateEmail(String email) {
        // email이 있는 경우에만 검증
        if (StringUtils.hasText(email)) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new InvalidUserDataException("올바르지 않은 이메일 형식입니다.");
            }
        }
    }
    
    public static void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new InvalidUserDataException("이름은 필수 입력값입니다.");
        }
        
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserDataException(
                String.format("이름은 %d자 이상 %d자 이하여야 합니다.", MIN_NAME_LENGTH, MAX_NAME_LENGTH));
        }
    }
    
    public static void validateNickname(String nickname) {
        if (StringUtils.hasText(nickname)) {
            if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
                throw new InvalidUserDataException("닉네임은 2-20자의 한글, 영문, 숫자만 사용 가능합니다.");
            }
        }
    }
    
    public static void validateProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new InvalidUserDataException("OAuth Provider는 필수 입력값입니다.");
        }
        
        // 지원하는 OAuth Provider 목록
        if (!"naver".equals(provider) && !"google".equals(provider) && !"kakao".equals(provider)) {
            throw new InvalidUserDataException("지원하지 않는 OAuth Provider입니다: " + provider);
        }
    }
    
    public static void validateProviderId(String providerId) {
        if (!StringUtils.hasText(providerId)) {
            throw new InvalidUserDataException("OAuth Provider ID는 필수 입력값입니다.");
        }
    }
    
    public static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidUserDataException("유효하지 않은 사용자 ID입니다.");
        }
    }
}
