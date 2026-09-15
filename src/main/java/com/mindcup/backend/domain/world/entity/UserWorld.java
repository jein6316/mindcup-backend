package com.mindcup.backend.domain.world.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_worlds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWorld {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userWorldId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String unlockedWorldLevel; // 사용자가 도달한 최고 레벨 (SMALL_CUP, LARGE_CUP 등)

    @Column(nullable = false, length = 30)
    private String displayWorldLevel; // 홈 화면에 실제 표현할 레벨 (2단계 정책: display = unlocked)

    @Column(nullable = false, length = 30)
    private String calculatedWorldLevel; // 최근 7일 평균 점수로만 산출된 실제 환산 레벨

    @Column(nullable = false)
    private Integer lastCalculatedClarityScore; // 마지막 시점에 계산된 7일 평균 맑음도

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public UserWorld(Long userId, String unlockedWorldLevel, String displayWorldLevel, String calculatedWorldLevel, Integer lastCalculatedClarityScore) {
        this.userId = userId;
        this.unlockedWorldLevel = unlockedWorldLevel;
        this.displayWorldLevel = displayWorldLevel;
        this.calculatedWorldLevel = calculatedWorldLevel;
        this.lastCalculatedClarityScore = lastCalculatedClarityScore;
    }

    public void updateWorldLevels(String unlockedWorldLevel, String displayWorldLevel, String calculatedWorldLevel, Integer lastCalculatedClarityScore) {
        this.unlockedWorldLevel = unlockedWorldLevel;
        this.displayWorldLevel = displayWorldLevel;
        this.calculatedWorldLevel = calculatedWorldLevel;
        this.lastCalculatedClarityScore = lastCalculatedClarityScore;
    }
}
