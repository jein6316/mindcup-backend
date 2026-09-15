package com.mindcup.backend.domain.mission.entity;

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
@Table(name = "daily_missions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    @Column(nullable = false, unique = true, length = 50)
    private String missionCode;

    @Column(nullable = false, length = 30)
    private String missionType; // SELF_CARE, REFLECTION, ENVIRONMENT, COMFORT, REST, MOVEMENT

    @Column(nullable = false, length = 100)
    private String titleKey;

    @Column(nullable = false, length = 255)
    private String descriptionKey;

    @Column(nullable = false)
    private Integer effectScore;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false, length = 1)
    private String useYn;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DailyMission(String missionCode, String missionType, String titleKey, String descriptionKey, Integer effectScore, Integer sortOrder, String useYn) {
        this.missionCode = missionCode;
        this.missionType = missionType;
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.effectScore = effectScore;
        this.sortOrder = sortOrder;
        this.useYn = useYn != null ? useYn : "Y";
        this.createdAt = LocalDateTime.now();
    }
}
