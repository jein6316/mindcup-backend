package com.mindcup.backend.domain.report.entity;

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
    name = "weekly_reports",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "startDate", "endDate"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklyReportId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer averageClarityScore;

    @Column(nullable = false)
    private Integer averagePollutionScore;

    @Column(length = 100)
    private String mostCommonCloudyAction;

    @Column(length = 100)
    private String mostCommonClearAction;

    @Column(length = 100)
    private String mostEffectiveClearAction;

    @Column(nullable = false)
    private Integer sentDropCount;

    @Column(nullable = false)
    private Integer pouredDropCount;

    @Column(nullable = false)
    private Integer completedMissionCount;

    @Column(nullable = false, length = 30)
    private String unlockedWorldLevel;

    @Column(nullable = false, length = 100)
    private String reportMessageKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public WeeklyReport(Long userId, LocalDate startDate, LocalDate endDate, Integer averageClarityScore, Integer averagePollutionScore, String mostCommonCloudyAction, String mostCommonClearAction, String mostEffectiveClearAction, Integer sentDropCount, Integer pouredDropCount, Integer completedMissionCount, String unlockedWorldLevel, String reportMessageKey) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.averageClarityScore = averageClarityScore;
        this.averagePollutionScore = averagePollutionScore;
        this.mostCommonCloudyAction = mostCommonCloudyAction;
        this.mostCommonClearAction = mostCommonClearAction;
        this.mostEffectiveClearAction = mostEffectiveClearAction;
        this.sentDropCount = sentDropCount != null ? sentDropCount : 0;
        this.pouredDropCount = pouredDropCount != null ? pouredDropCount : 0;
        this.completedMissionCount = completedMissionCount != null ? completedMissionCount : 0;
        this.unlockedWorldLevel = unlockedWorldLevel;
        this.reportMessageKey = reportMessageKey;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateReport(Integer averageClarityScore, Integer averagePollutionScore, String mostCommonCloudyAction, String mostCommonClearAction, String mostEffectiveClearAction, Integer sentDropCount, Integer pouredDropCount, Integer completedMissionCount, String unlockedWorldLevel, String reportMessageKey) {
        this.averageClarityScore = averageClarityScore;
        this.averagePollutionScore = averagePollutionScore;
        this.mostCommonCloudyAction = mostCommonCloudyAction;
        this.mostCommonClearAction = mostCommonClearAction;
        this.mostEffectiveClearAction = mostEffectiveClearAction;
        this.sentDropCount = sentDropCount;
        this.pouredDropCount = pouredDropCount;
        this.completedMissionCount = completedMissionCount;
        this.unlockedWorldLevel = unlockedWorldLevel;
        this.reportMessageKey = reportMessageKey;
        this.updatedAt = LocalDateTime.now();
    }
}
