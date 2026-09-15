package com.mindcup.backend.domain.user.dto;

import com.mindcup.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String friendCode;
    private String languageSetting;
    private String unlockedWorldLevel;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getFriendCode(),
                user.getLanguageSetting(),
                user.getUnlockedWorldLevel()
        );
    }
}
