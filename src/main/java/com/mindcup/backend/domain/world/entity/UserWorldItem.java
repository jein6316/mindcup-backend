package com.mindcup.backend.domain.world.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_world_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWorldItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userWorldItemId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 1)
    private String equippedYn; // 장착 여부 (Y/N)

    @Column(nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.acquiredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public UserWorldItem(Long userId, Long itemId, String equippedYn) {
        this.userId = userId;
        this.itemId = itemId;
        this.equippedYn = equippedYn;
    }

    public void updateEquippedStatus(String equippedYn) {
        this.equippedYn = equippedYn;
    }
}
