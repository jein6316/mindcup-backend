package com.mindcup.backend.domain.friend.entity;

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
@Table(name = "user_friend_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFriendSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String visibilityLevel; // PRIVATE, RECORD_STATUS_ONLY, CUP_LEVEL_ONLY

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserFriendSetting(Long userId, String visibilityLevel) {
        this.userId = userId;
        this.visibilityLevel = visibilityLevel != null ? visibilityLevel : "RECORD_STATUS_ONLY";
        this.createdAt = LocalDateTime.now();
    }

    public void updateVisibilityLevel(String visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }
}
