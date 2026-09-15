package com.mindcup.backend.domain.mission.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_daily_missions",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "missionId", "missionDate"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userDailyMissionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long missionId;

    @Column(nullable = false)
    private LocalDate missionDate;

    @Column(nullable = false, length = 1)
    private String completedYn; // Y or N

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserDailyMission(Long userId, Long missionId, LocalDate missionDate) {
        this.userId = userId;
        this.missionId = missionId;
        this.missionDate = missionDate;
        this.completedYn = "N";
        this.createdAt = LocalDateTime.now();
    }

    public void complete() {
        this.completedYn = "Y";
        this.completedAt = LocalDateTime.now();
    }
}
