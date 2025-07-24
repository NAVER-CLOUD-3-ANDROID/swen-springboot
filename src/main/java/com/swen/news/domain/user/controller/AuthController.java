package com.swen.news.domain.user.controller;

import com.swen.news.domain.user.dto.UserResponseDto;
import com.swen.news.domain.user.dto.UserUpdateDto;
import com.swen.news.domain.user.entity.User;
import com.swen.news.domain.user.service.RefreshTokenService;
import com.swen.news.domain.user.service.UserService;
import com.swen.news.domain.user.exception.UserNotFoundException;
import com.swen.news.global.jwt.JwtProvider;
import com.swen.news.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/naver")
    public ResponseEntity<ApiResponse<Map<String, String>>> naverLogin() {
        // 네이버 로그인 URL로 리다이렉트
        Map<String, String> response = Map.of(
                "loginUrl", "/oauth2/authorization/naver",
                "message", "네이버 로그인 URL입니다."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(Authentication authentication) {

        log.info("🔍 [로그인] 현재 사용자 정보 조회 시작");

        if (authentication == null) {
            log.warn("⚠️ [로그인] 인증되지 않은 사용자");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증되지 않은 사용자입니다.", "UNAUTHORIZED"));
        }

        log.info("✅ [로그인] 인증 성공 - 사용자: {}", authentication.getName());
        log.info("📋 [로그인] 사용자 정보: {}", authentication.getPrincipal());

        return ResponseEntity.ok(ApiResponse.success(authentication.getPrincipal()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long userId) {
        log.info("사용자 정보 조회 - UserId: {}", userId);

        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            throw UserNotFoundException.forUserId(userId);
        }

        UserResponseDto userResponse = UserResponseDto.from(userOptional.get());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmail(@PathVariable String email) {
        log.info("이메일로 사용자 정보 조회 - Email: {}", email);

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw UserNotFoundException.forEmail(email);
        }

        UserResponseDto userResponse = UserResponseDto.from(userOptional.get());
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDto updateDto) {

        log.info("사용자 정보 업데이트 - UserId: {}, UpdateDto: {}", userId, updateDto);

        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            throw UserNotFoundException.forUserId(userId);
        }

        User user = userOptional.get();

        // 업데이트할 필드만 변경
        if (updateDto.hasName()) {
            user.setName(updateDto.getName());
        }
        if (updateDto.hasNickname()) {
            user.setNickname(updateDto.getNickname());
        }
        if (updateDto.hasProfileImageUrl()) {
            user.setProfileImageUrl(updateDto.getProfileImageUrl());
        }
        if (updateDto.hasMobile()) {
            user.setMobile(updateDto.getMobile());
        }

        User updatedUser = userService.updateUser(user);
        UserResponseDto userResponse = UserResponseDto.from(updatedUser);

        return ResponseEntity.ok(ApiResponse.success(userResponse, "사용자 정보가 업데이트되었습니다."));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long userId) {
        log.info("사용자 비활성화 - UserId: {}", userId);

        userService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "사용자가 비활성화되었습니다."));
    }

    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserExists(@PathVariable Long userId) {
        log.info("사용자 존재 여부 확인 - UserId: {}", userId);

        Optional<User> userOptional = userService.findById(userId);
        Map<String, Boolean> response = Map.of("exists", userOptional.isPresent());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/email/{email}/exists")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmailExists(@PathVariable String email) {
        log.info("이메일 존재 여부 확인 - Email: {}", email);

        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = Map.of("exists", exists);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthStatus() {
        Map<String, String> status = Map.of(
                "status", "OAuth2 설정 완료",
                "provider", "naver",
                "version", "1.0"
        );

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Map<String, String>>> issueJwtTokens(@RequestBody Map<String, Object> userAttributes) {

        log.info("🔑 [토큰] JWT 토큰 발급 요청");
        log.info("📝 [토큰] 받은 데이터: {}", userAttributes);


        try {
            // 네이버에서 전달받은 사용자 정보 예시: 이메일, 닉네임, 프로필 등
            String email = (String) userAttributes.get("email");
            String nickname = (String) userAttributes.get("nickname");

            // 사용자 정보 확인(또는 생성)
            User user = userService.findByEmail(email)
                    .orElse(userService.createSocialUser(email, nickname));

            // JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(user.getEmail());
            String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

            // refreshToken 저장(예: Redis, DB)
            userService.saveRefreshToken(user.getId(), refreshToken);

            // 응답 반환
            Map<String, String> tokens = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );

            log.info("✅ [토큰] 발급 완료 - 사용자: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success(tokens));

        } catch (Exception e) {
            log.error("❌ [토큰] 발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("토큰 발급 실패", "TOKEN_ERROR"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshJwtTokens(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");

        // 1. RefreshToken 검증
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("유효하지 않은 리프레시 토큰입니다.", "UNAUTHORIZED"));
        }

        try {
            // 2. RefreshTokenService 사용 (DB에서 토큰 조회 + 새 AccessToken 생성)
            String newAccessToken = refreshTokenService.refreshAccessToken(refreshToken);

            // 3. 응답 생성
            Map<String, String> response = Map.of("accessToken", newAccessToken);
            return ResponseEntity.ok(ApiResponse.success(response, "Access 토큰이 재발급되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("토큰 재발급에 실패했습니다.", "TOKEN_REFRESH_FAILED"));
        }
    }
}
