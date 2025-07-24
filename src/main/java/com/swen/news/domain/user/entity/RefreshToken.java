package com.swen.news.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", unique = true, nullable = false, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 토큰 만료 여부 확인 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 토큰 폐기 메서드
    public void revoke() {
        this.isRevoked = true;
    }

    // 토큰 유효성 확인 (만료되지 않고 폐기되지 않음)
    public boolean isValid() {
        return !isExpired() && !isRevoked;
    }
}
