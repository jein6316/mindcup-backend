package com.mindcup.backend.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class WeeklyReportResponse {
    private Long weeklyReportId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer averageClarityScore;
    private Integer averagePollutionScore;
    private String mostCommonCloudyAction;
    private String mostCommonClearAction;
    private String mostEffectiveClearAction;
    private Integer sentDropCount;
    private Integer pouredDropCount;
    private Integer completedMissionCount;
    private String unlockedWorldLevel;
    private String reportMessage; // 다국어 변환된 회고 격려 메시지
}
