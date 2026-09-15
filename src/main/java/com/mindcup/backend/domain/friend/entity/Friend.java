package com.mindcup.backend.domain.friend.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(
    name = "friends",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "targetUserId"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendId;

    @Column(nullable = false)
    private Long userId; // 요청 송신자

    @Column(nullable = false)
    private Long targetUserId; // 요청 수신자

    @Column(nullable = false, length = 20)
    private String status; // PENDING, ACCEPTED, REJECTED, BLOCKED

    @Column
    private Long blockedByUserId; // 차단한 사용자 ID

    @Column
    private LocalDateTime blockedAt; // 차단 시점

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Friend(Long userId, Long targetUserId, String status) {
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void block(Long blockedByUserId) {
        this.status = "BLOCKED";
        this.blockedByUserId = blockedByUserId;
        this.blockedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
