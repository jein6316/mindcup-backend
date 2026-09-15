package com.mindcup.backend.domain.cup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "mind_score_effect_logs",
    indexes = {
        @Index(name = "idx_effect_logs_lookup", columnList = "userId, effectDate, sourceType")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MindScoreEffectLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String sourceType; // CLOUDY_ACTION, CLEAR_ACTION, COMFORT_SENT, COMFORT_POURED

    @Column(nullable = false)
    private Long sourceId; // comfortMessageId, actionLogId 등 원천 ID

    @Column(nullable = false)
    private Integer effectScore; // pollutionScore 변동량 (감소는 음수, 상승은 양수)

    @Column(nullable = false)
    private LocalDate effectDate; // 서버 LocalDate 기준 일자

    @Column(nullable = false, length = 1)
    private String limitExceededYn; // 일일 한도 초과 여부 (Y/N)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MindScoreEffectLog(Long userId, String sourceType, Long sourceId, Integer effectScore, LocalDate effectDate, String limitExceededYn) {
        this.userId = userId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.effectScore = effectScore;
        this.effectDate = effectDate;
        this.limitExceededYn = limitExceededYn;
        this.createdAt = LocalDateTime.now();
    }
}
