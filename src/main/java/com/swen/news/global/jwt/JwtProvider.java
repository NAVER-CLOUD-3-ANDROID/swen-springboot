package com.swen.news.global.jwt;

import com.swen.news.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j  // ← 이 어노테이션을 추가!
public class JwtProvider {
    private final Key key;

    // 🔑 생성자 - JWT 서명/검증용 키 초기화 (필수!)
    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        log.info("✅ [JWT] JWT Provider 초기화 완료");
    }

    // 🎫 Access Token 생성 (30분)
    public String createAccessToken(String subject) {
        long now = System.currentTimeMillis();
        long tokenValidityInMilliseconds = 1000 * 60 * 30; // 30분

        log.debug("🔑 [JWT] AccessToken 생성 - Subject: {}", subject);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + tokenValidityInMilliseconds))
                .signWith(key)
                .compact();
    }

    // 🔍 범용 토큰 검증 (Access/Refresh 구분 없이)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()  // parserBuilder() 대신 parser() 사용 (현재 코드와 일치)
                    .setSigningKey(key)  // getSigningKey() 대신 key 사용 (이미 필드로 가지고 있음)
                    .build()
                    .parseClaimsJws(token);

            log.debug("🔍 [JWT] Token 검증 성공");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ [JWT] 만료된 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("⚠️ [JWT] 지원되지 않는 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("⚠️ [JWT] 잘못된 형식의 토큰: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("⚠️ [JWT] 잘못된 서명: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ [JWT] 잘못된 토큰 인자: {}", e.getMessage());
            return false;
        }
    }

    // 🔄 Refresh Token 생성 (7일)
    public String createRefreshToken(String subject) {
        long now = System.currentTimeMillis();
        long refreshTokenValidityInMilliseconds = 1000 * 60 * 60 * 24 * 7; // 7일

        log.debug("🔄 [JWT] RefreshToken 생성 - Subject: {}", subject);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshTokenValidityInMilliseconds))
                .signWith(key)
                .compact();
    }

    // ✅ Refresh Token 검증
    public boolean validateRefreshToken(String refreshToken) {
        try {
            // 파싱 및 서명검증(파서 생성 → setSigningKey → build → 파싱)
            Claims claims = Jwts.parser()
                    .setSigningKey(key)  // key는 JwtProvider에서 관리하는 HMAC용 key
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            // 만료기간 체크
            boolean isValid = claims.getExpiration().after(new Date());
            log.debug("🔍 [JWT] RefreshToken 검증 결과: {}", isValid);
            return isValid;

        } catch (JwtException | IllegalArgumentException e) {
            // 시그니처 위조, 만료, 파싱 오류 등
            log.warn("⚠️ [JWT] RefreshToken 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 🆔 토큰에서 Subject(사용자 ID) 추출
    public String getSubject(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("⚠️ [JWT] Subject 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 🆔 사용자 ID를 Long 타입으로 반환
    public Long getUserId(String token) {
        String subject = getSubject(token);
        if (subject != null) {
            try {
                return Long.valueOf(subject);
            } catch (NumberFormatException e) {
                log.warn("⚠️ [JWT] Subject를 Long으로 변환 실패: {}", subject);
            }
        }
        return null;
    }
}