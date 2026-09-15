package com.mindcup.backend.domain.cup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mind_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MindRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cup_id", nullable = false)
    private DailyCup dailyCup;

    @Column(nullable = false)
    private String recordType; // CLEAR, TURBID

    @Column(nullable = false)
    private String actionName;

    @Column(nullable = false)
    private Integer intensity; // 1 ~ 5

    @Column(length = 1000)
    private String memo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MindRecord(DailyCup dailyCup, String recordType, String actionName, Integer intensity, String memo) {
        this.dailyCup = dailyCup;
        this.recordType = recordType;
        this.actionName = actionName;
        this.intensity = intensity;
        this.memo = memo;
    }
}
