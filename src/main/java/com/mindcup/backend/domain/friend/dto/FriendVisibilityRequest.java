package com.mindcup.backend.domain.friend.dto;

import com.mindcup.backend.domain.friend.entity.FriendVisibilityLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendVisibilityRequest {

    @NotNull(message = "공개 범위 설정은 필수입니다.")
    private FriendVisibilityLevel visibilityLevel;
}
