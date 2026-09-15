package com.mindcup.backend.domain.comfort.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comfort_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComfortMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comfortId;

    @Column(nullable = false)
    private Long senderUserId;

    @Column(nullable = false)
    private Long receiverUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "templateId", insertable = false, updatable = false)
    private ComfortMessageTemplate template;

    @Column(nullable = false)
    private Long templateId;

    @Column(nullable = false, length = 1)
    private String readYn; // Y or N

    @Column(nullable = false, length = 1)
    private String pouredYn; // Y or N

    @Column(nullable = false)
    private Integer senderEffectScore; // 송신 당시 차감된 점수 (0 또는 2)

    @Column(nullable = false)
    private Integer receiverEffectScore; // 수신 후 부었을 때 적용할 점수 (3)

    @Column(nullable = false)
    private LocalDate sentDate; // 중복 발송 제한용 서버 로컬 일자

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime pouredAt;

    @Builder
    public ComfortMessage(Long senderUserId, Long receiverUserId, Long templateId, Integer senderEffectScore, Integer receiverEffectScore) {
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.templateId = templateId;
        this.readYn = "N";
        this.pouredYn = "N";
        this.senderEffectScore = senderEffectScore;
        this.receiverEffectScore = receiverEffectScore;
        this.sentDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.readYn = "Y";
    }

    public void pour() {
        this.pouredYn = "Y";
        this.pouredAt = LocalDateTime.now();
    }
}
