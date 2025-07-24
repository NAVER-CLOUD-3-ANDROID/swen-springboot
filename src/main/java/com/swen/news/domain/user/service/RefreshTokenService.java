package com.swen.news.domain.user.service;

import com.swen.news.domain.user.entity.RefreshToken;
import com.swen.news.domain.user.repository.RefreshTokenRepository;
import com.swen.news.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider; // 당신의 JwtProvider 클래스명

    // RefreshToken 생성
    public RefreshToken createRefreshToken(Long userId) {
        log.debug("🔄 RefreshToken 생성 시작 - UserId: {}", userId);

        // 기존 토큰이 있다면 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // JWT RefreshToken 생성
        String tokenValue = jwtProvider.createRefreshToken(String.valueOf(userId));
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7일 후 만료

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(expiresAt)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("✅ RefreshToken 생성 완료 - UserId: {}", userId);
        return saved;
    }

    // RefreshToken으로 AccessToken 재발급
    public String refreshAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 리프레시 토큰"));

        if (!token.isValid()) {
            throw new RuntimeException("만료되거나 폐기된 리프레시 토큰");
        }

        return jwtProvider.createAccessToken(String.valueOf(token.getUserId()));
    }
}
