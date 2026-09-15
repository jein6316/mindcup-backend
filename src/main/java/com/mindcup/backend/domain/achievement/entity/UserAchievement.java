package com.mindcup.backend.domain.achievement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_achievements",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "achievementId"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userAchievementId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long achievementId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime achievedAt;

    @Builder
    public UserAchievement(Long userId, Long achievementId) {
        this.userId = userId;
        this.achievementId = achievementId;
        this.achievedAt = LocalDateTime.now();
    }
}
