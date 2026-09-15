package com.mindcup.backend.domain.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AchievementResponse {
    private Long achievementId;
    private String achievementCode;
    private String title;
    private String description;
    private Boolean achievedYn;
    private LocalDateTime achievedAt;
}
