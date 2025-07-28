package com.swen.news.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    
    private String name;
    private String nickname;
    private String profileImageUrl;
    private String mobile;
    
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }
    
    public boolean hasNickname() {
        return nickname != null && !nickname.trim().isEmpty();
    }
    
    public boolean hasProfileImageUrl() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    public boolean hasMobile() { return mobile != null && !mobile.trim().isEmpty(); }
}
