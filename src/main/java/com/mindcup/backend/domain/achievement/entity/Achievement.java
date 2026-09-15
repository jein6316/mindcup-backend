package com.mindcup.backend.domain.achievement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long achievementId;

    @Column(nullable = false, unique = true, length = 50)
    private String achievementCode;

    @Column(nullable = false, length = 100)
    private String titleKey;

    @Column(nullable = false, length = 255)
    private String descriptionKey;

    @Column(nullable = false, length = 50)
    private String conditionType; // CLEAR_ACTION_COUNT, WORLD_UNLOCKED 등

    @Column(nullable = false)
    private Integer conditionValue;

    @Column(length = 30)
    private String rewardType;

    @Column
    private Integer rewardValue;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false, length = 1)
    private String useYn;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Achievement(String achievementCode, String titleKey, String descriptionKey, String conditionType, Integer conditionValue, String rewardType, Integer rewardValue, Integer sortOrder, String useYn) {
        this.achievementCode = achievementCode;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.sortOrder = sortOrder;
        this.useYn = useYn != null ? useYn : "Y";
        this.createdAt = LocalDateTime.now();
    }
}
