package com.swen.news.domain.user.entity;

import com.swen.news.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    // OAuth 관련 필드
    @Column(name = "provider")
    private String provider; // "naver", "google" 등

    @Column(name = "provider_id")
    private String providerId; // OAuth 제공자의 사용자 ID

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
