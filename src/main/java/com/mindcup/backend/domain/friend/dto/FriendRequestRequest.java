package com.mindcup.backend.domain.friend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestRequest {

    @NotBlank(message = "친구 코드는 필수입니다.")
    private String targetFriendCode;
}
