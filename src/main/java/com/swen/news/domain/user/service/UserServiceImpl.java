package com.swen.news.domain.user.service;

import com.swen.news.domain.user.entity.User;
import com.swen.news.domain.user.repository.UserRepository;
import com.swen.news.domain.user.exception.InvalidUserDataException;
import com.swen.news.domain.user.exception.UserNotFoundException;
import com.swen.news.domain.user.exception.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.swen.news.domain.user.validator.UserValidator;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User processOAuthUser(String provider, String providerId, String email, 
                               String name, String nickname, String profileImage) {
        
        log.info("OAuth 사용자 처리 시작 - Provider: {}, ProviderId: {}, Email: {}", provider, providerId, email);
        
        try {
            // 입력 데이터 유효성 검증
            validateOAuthUserData(provider, providerId, email, name);
            
            // 기존 사용자 확인 (provider + providerId로)
            Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
            
            if (existingUser.isPresent()) {
                // 기존 사용자 정보 업데이트
                User user = existingUser.get();
                updateUserInfo(user, name, nickname, profileImage);
                
                User savedUser = userRepository.save(user);
                log.info("기존 사용자 정보 업데이트 완료 - UserId: {}", savedUser.getId());
                return savedUser;
            } else {
                // 새 사용자 생성
                User newUser = createNewUser(provider, providerId, email, name, nickname, profileImage);
                User savedUser = userRepository.save(newUser);
                log.info("새 사용자 생성 완료 - UserId: {}", savedUser.getId());
                return savedUser;
            }
        } catch (DataAccessException e) {
            log.error("데이터베이스 오류 발생 - Provider: {}, ProviderId: {}", provider, providerId, e);
            throw new UserServiceException("사용자 정보 처리 중 데이터베이스 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("OAuth 사용자 처리 중 예상치 못한 오류 발생 - Provider: {}, ProviderId: {}", provider, providerId, e);
            throw new UserServiceException("OAuth 사용자 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("이메일로 사용자 조회 - Email: {}", email);
        
        UserValidator.validateEmail(email);
        
        try {
            return userRepository.findByEmail(email);
        } catch (DataAccessException e) {
            log.error("이메일로 사용자 조회 중 데이터베이스 오류 발생 - Email: {}", email, e);
            throw new UserServiceException("사용자 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("ID로 사용자 조회 - ID: {}", id);
        
        UserValidator.validateUserId(id);
        
        try {
            return userRepository.findById(id);
        } catch (DataAccessException e) {
            log.error("ID로 사용자 조회 중 데이터베이스 오류 발생 - ID: {}", id, e);
            throw new UserServiceException("사용자 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        log.debug("Provider와 ProviderId로 사용자 조회 - Provider: {}, ProviderId: {}", provider, providerId);
        
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(providerId)) {
            throw new InvalidUserDataException("Provider와 ProviderId는 필수 입력값입니다.");
        }
        
        try {
            return userRepository.findByProviderAndProviderId(provider, providerId);
        } catch (DataAccessException e) {
            log.error("Provider와 ProviderId로 사용자 조회 중 데이터베이스 오류 발생 - Provider: {}, ProviderId: {}", provider, providerId, e);
            throw new UserServiceException("사용자 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public User updateUser(User user) {
        log.info("사용자 정보 업데이트 - UserId: {}", user.getId());
        
        if (user == null || user.getId() == null) {
            throw new InvalidUserDataException("업데이트할 사용자 정보가 올바르지 않습니다.");
        }
        
        try {
            if (!userRepository.existsById(user.getId())) {
                throw UserNotFoundException.forUserId(user.getId());
            }
            
            return userRepository.save(user);
        } catch (DataAccessException e) {
            log.error("사용자 정보 업데이트 중 데이터베이스 오류 발생 - UserId: {}", user.getId(), e);
            throw new UserServiceException("사용자 정보 업데이트 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    public void deactivateUser(Long userId) {
        log.info("사용자 비활성화 - UserId: {}", userId);
        
        UserValidator.validateUserId(userId);
        
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw UserNotFoundException.forUserId(userId);
            }
            
            User user = userOptional.get();
            if (!user.getIsActive()) {
                log.warn("이미 비활성화된 사용자입니다 - UserId: {}", userId);
                return;
            }
            
            user.setIsActive(false);
            userRepository.save(user);
            log.info("사용자 비활성화 완료 - UserId: {}", userId);
            
        } catch (DataAccessException e) {
            log.error("사용자 비활성화 중 데이터베이스 오류 발생 - UserId: {}", userId, e);
            throw new UserServiceException("사용자 비활성화 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("이메일 존재 여부 확인 - Email: {}", email);
        
        UserValidator.validateEmail(email);
        
        try {
            return userRepository.existsByEmail(email);
        } catch (DataAccessException e) {
            log.error("이메일 존재 여부 확인 중 데이터베이스 오류 발생 - Email: {}", email, e);
            throw new UserServiceException("이메일 존재 여부 확인 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    // Private 헬퍼 메서드들
    
    private void validateOAuthUserData(String provider, String providerId, String email, String name) {
        UserValidator.validateProvider(provider);
        UserValidator.validateProviderId(providerId);
        // email은 선택사항이므로 검증하지 않음
        UserValidator.validateName(name);
    }
    
    private void updateUserInfo(User user, String name, String nickname, String profileImage) {
        if (StringUtils.hasText(name)) {
            UserValidator.validateName(name);
            user.setName(name);
        }
        if (StringUtils.hasText(nickname)) {
            UserValidator.validateNickname(nickname);
            user.setNickname(nickname);
        }
        if (StringUtils.hasText(profileImage)) {
            user.setProfileImageUrl(profileImage);
        }
    }
    
    private User createNewUser(String provider, String providerId, String email, 
                             String name, String nickname, String profileImage) {
        return User.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .profileImageUrl(profileImage)
                .isActive(true)
                .build();
    }
}
