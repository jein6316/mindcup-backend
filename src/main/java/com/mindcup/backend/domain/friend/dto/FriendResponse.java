package com.mindcup.backend.domain.friend.dto;

import com.mindcup.backend.domain.friend.entity.FriendVisibilityLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendResponse {
    private Long friendId;
    private Long friendUserId;
    private String nickname;
    private String email;
    private String status; // PENDING, ACCEPTED
    private Boolean todayRecorded; // RECORD_STATUS_ONLY 이상
    private String activeWorldLevel; // CUP_LEVEL_ONLY
    private String cupLevel; // CUP_LEVEL_ONLY (CLEAR, SLIGHTLY_CLOUDY 등)

    public static FriendResponse of(
            Long friendId,
            Long friendUserId,
            String nickname,
            String email,
            String status,
            FriendVisibilityLevel visibilityLevel,
            Boolean hasRecordedToday,
            String activeWorldLevel,
            String cupLevel) {
        
        FriendResponseBuilder builder = FriendResponse.builder()
                .friendId(friendId)
                .friendUserId(friendUserId)
                .nickname(nickname)
                .email(email)
                .status(status);

        if (visibilityLevel == FriendVisibilityLevel.PRIVATE) {
            builder.todayRecorded(null)
                   .activeWorldLevel(null)
                   .cupLevel(null);
        } else if (visibilityLevel == FriendVisibilityLevel.RECORD_STATUS_ONLY) {
            builder.todayRecorded(hasRecordedToday)
                   .activeWorldLevel(null)
                   .cupLevel(null);
        } else if (visibilityLevel == FriendVisibilityLevel.CUP_LEVEL_ONLY) {
            builder.todayRecorded(hasRecordedToday)
                   .activeWorldLevel(activeWorldLevel)
                   .cupLevel(cupLevel);
        }

        return builder.build();
    }
}
