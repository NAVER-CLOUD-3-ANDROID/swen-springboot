package com.swen.news.domain.user.dto;

import com.swen.news.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String gender;
    private String birthday;
    private String mobile;
    private String provider;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .mobile(user.getMobile())
                .provider(user.getProvider())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
