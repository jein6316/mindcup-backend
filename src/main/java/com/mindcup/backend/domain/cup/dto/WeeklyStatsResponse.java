package com.mindcup.backend.domain.cup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class WeeklyStatsResponse {
    private Double averageClarity;
    private String highestWorldLevel;
    private List<DailyStat> history;

    @Getter
    @AllArgsConstructor
    public static class DailyStat {
        private LocalDate date;
        private Integer clarityScore;
    }
}
