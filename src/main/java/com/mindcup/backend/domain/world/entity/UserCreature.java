package com.mindcup.backend.domain.world.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_creatures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCreature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userCreatureId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long creatureId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @PrePersist
    protected void onCreate() {
        this.acquiredAt = LocalDateTime.now();
    }

    @Builder
    public UserCreature(Long userId, Long creatureId) {
        this.userId = userId;
        this.creatureId = creatureId;
    }
}
