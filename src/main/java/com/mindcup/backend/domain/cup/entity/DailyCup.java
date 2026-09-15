package com.mindcup.backend.domain.cup.entity;

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

@Entity
@Table(
    name = "daily_cups",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "recordDate"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cupId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private Integer pollutionScore; // 0 ~ 100

    @Column(nullable = false)
    private Integer clarityScore; // 100 - pollutionScore

    @Column(nullable = false)
    private String activeWorldLevel; // SMALL_CUP, LARGE_CUP ...

    @Column(nullable = false)
    private Integer todaySentDropPoints; // 오늘 보낸 물방울 감쇄 누적 (최대 10)

    @Column(nullable = false)
    private Integer todayPouredDropPoints; // 오늘 부은 물방울 감쇄 누적 (최대 15)

    @Builder
    public DailyCup(Long userId, LocalDate recordDate, Integer pollutionScore, String activeWorldLevel) {
        this.userId = userId;
        this.recordDate = recordDate;
        this.pollutionScore = pollutionScore;
        this.clarityScore = 100 - pollutionScore;
        this.activeWorldLevel = activeWorldLevel;
        this.todaySentDropPoints = 0;
        this.todayPouredDropPoints = 0;
    }

    public void updatePollutionScore(Integer newPollutionScore) {
        this.pollutionScore = Math.max(0, Math.min(100, newPollutionScore));
        this.clarityScore = 100 - this.pollutionScore;
    }

    public void addSentDropPoints(Integer points) {
        this.todaySentDropPoints += points;
    }

    public void addPouredDropPoints(Integer points) {
        this.todayPouredDropPoints += points;
    }

    public void updateActiveWorldLevel(String activeWorldLevel) {
        this.activeWorldLevel = activeWorldLevel;
    }

    public String getCupStatus() {
        if (pollutionScore <= 20) {
            return "CLEAR";
        } else if (pollutionScore <= 40) {
            return "SLIGHTLY_CLOUDY";
        } else if (pollutionScore <= 70) {
            return "CLOUDY";
        } else {
            return "VERY_CLOUDY";
        }
    }
}
