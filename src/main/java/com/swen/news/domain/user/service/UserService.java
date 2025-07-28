package com.swen.news.domain.user.service;

import com.swen.news.domain.user.entity.User;

import java.util.Optional;

public interface UserService {

    /**
     * OAuth 사용자 정보를 처리하여 사용자를 생성하거나 업데이트합니다.
     *
     * @param provider OAuth 제공자 (naver, google 등)
     * @param providerId OAuth 제공자의 사용자 ID
     * @param email 사용자 이메일
     * @param name 사용자 이름
     * @param nickname 사용자 닉네임
     * @param profileImage 프로필 이미지 URL
     * @return 처리된 사용자 정보
     */
    User processOAuthUser(String provider, String providerId, String email,
                         String name, String nickname, String profileImage,
                          String gender, String birthday, String mobile);

    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 조회할 이메일
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * ID로 사용자를 조회합니다.
     *
     * @param id 조회할 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findById(Long id);

    /**
     * Provider와 ProviderId로 사용자를 조회합니다.
     *
     * @param provider OAuth 제공자
     * @param providerId OAuth 제공자의 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * 사용자 정보를 업데이트합니다.
     *
     * @param user 업데이트할 사용자 정보
     * @return 업데이트된 사용자 정보
     */
    User updateUser(User user);

    /**
     * 사용자를 비활성화합니다.
     *
     * @param userId 비활성화할 사용자 ID
     */
    void deactivateUser(Long userId);

    /**
     * 이메일 존재 여부를 확인합니다.
     *
     * @param email 확인할 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    User createSocialUser(String email, String nickname);

    void saveRefreshToken(Long userId, String refreshToken);

    User findByRefreshToken(String refreshToken);
}
