package com.swen.news.global.config;

import com.swen.news.domain.user.entity.RefreshToken;
import com.swen.news.domain.user.entity.User;
import com.swen.news.domain.user.service.RefreshTokenService;
import com.swen.news.domain.user.service.UserService;
import com.swen.news.domain.user.exception.UserServiceException;
import com.swen.news.global.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
//        log.info("=== OAuth2 로그인 성공 핸들러 시작 ===");
//        log.info("Request URI: {}", request.getRequestURI());
//        log.info("Request URL: {}", request.getRequestURL());
//        log.info("Authentication: {}", authentication.getClass().getSimpleName());
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String provider = "naver";
            
            log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());
            
            // 네이버 사용자 정보 추출
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
            
            log.info("Naver response map: {}", responseMap);
            
            if (responseMap == null) {
                log.error("네이버 OAuth2 응답에서 사용자 정보를 찾을 수 없습니다.");
                response.sendRedirect("http://localhost:3000/login?error=invalid_response");
                return;
            }
            
            String providerId = (String) responseMap.get("id");
            String email = (String) responseMap.get("email");
            String name = (String) responseMap.get("name");
            String nickname = (String) responseMap.get("nickname");
            String profileImage = (String) responseMap.get("profile_image");
            String gender = (String) responseMap.get("gender");
            String birthday = (String) responseMap.get("birthday");
            String mobile = (String) responseMap.get("mobile");

            log.info("OAuth2 로그인 성공 - Provider: {}, ProviderId: {}, Email: {}", provider, providerId, email);

            // 필수 정보 검증 (providerId, name만 필수)
            if (providerId == null || name == null) {
                log.error("필수 사용자 정보가 누락되었습니다. ProviderId: {}, Name: {}", providerId, name);
                response.sendRedirect("http://localhost:3000/login?error=missing_required_info");
                return;
            }

            // 사용자 정보 저장 또는 업데이트
            User user = userService.processOAuthUser(provider, providerId, email, name, nickname, profileImage, gender, birthday, mobile);
            
            log.info("사용자 처리 완료 - UserId: {}", user.getId());
            
            // TODO: JWT 토큰 생성 로직 추가 시 활성화
            String accessToken = jwtProvider.createAccessToken(String.valueOf(user.getId()));

            // 2. RefreshToken 생성 및 DB 저장
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // 3. 클라이언트에 전달
            response.addCookie(createCookie("accessToken", accessToken));
            response.addCookie(createCookie("refreshToken", refreshToken.getToken()));
            
            // 임시로 성공 메시지 표시 (프론트엔드 없이 테스트)
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write("""
                <html>
                <head><title>로그인 성공</title></head>
                <body>
                    <h2>🎉 네이버 로그인 성공!</h2>
                    <p><strong>사용자 ID:</strong> %d</p>
                    <p><strong>이름:</strong> %s</p>
                    <p><strong>이메일:</strong> %s</p>
                    <p><strong>닉네임:</strong> %s</p>
                    <p><strong>프로필 사진:</strong> %s</p>
                    <p><strong>성별:</strong> %s</p>
                    <p><strong>휴대전화번호:</strong> %s</p>
                    <p><strong>생일:</strong> %s</p>
                    <hr>
                    <p><a href="/api/auth/user/%d">사용자 정보 조회</a></p>
                    <p><a href="/swagger-ui.html">API 문서</a></p>
                </body>
                </html>
                """.formatted(
                    user.getId(),               // 사용자 ID (%d)
                    user.getName(),             // 이름 (%s)
                    user.getEmail(),            // 이메일 (%s)
                    user.getNickname(),         // 닉네임 (%s)
                    user.getProfileImageUrl(),  // 프로필 사진 (%s)
                    user.getGender(),           // 성별 (%s)
                    user.getBirthday(),         // 생일 (%s)
                    user.getMobile(),           // 휴대전화번호 (%s)
                    user.getId()                // URL의 사용자 ID (%d)
                    ));
            
        } catch (UserServiceException e) {
            log.error("OAuth2 로그인 처리 중 사용자 서비스 오류 발생: {}", e.getMessage(), e);
            response.sendRedirect("http://localhost:3000/login?error=user_service_error");
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            response.sendRedirect("http://localhost:3000/login?error=unknown_error");
        }
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // XSS 공격 방지
        cookie.setSecure(false);   // 개발환경에서는 false, 운영환경에서는 true
        cookie.setPath("/");       // 전체 경로에서 사용
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일 (초 단위)
        return cookie;
    }
}
