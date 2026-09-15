package com.mindcup.backend.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FriendRequestResponse {
    private Long friendId; // Relation ID
    private Long requesterUserId;
    private String nickname;
    private String email;
    private LocalDateTime createdAt;
}
