package com.mindcup.backend.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FriendMindStatusResponse {
    private Long friendUserId;
    private String nickname;
    private String profileImageUrl;
    private String visibilityLevel;
    private Boolean recordedToday;
    private String mindCupLevel; // CLEAR, SLIGHTLY_CLOUDY, CLOUDY, VERY_CLOUDY
    private Boolean canSendComfort;
    private Boolean alreadySentDropToday;
    private String messageKey;
}
