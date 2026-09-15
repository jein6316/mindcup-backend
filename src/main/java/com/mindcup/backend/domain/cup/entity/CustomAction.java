package com.mindcup.backend.domain.cup.entity;

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

@Entity
@Table(name = "custom_actions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String actionName;

    @Column(nullable = false)
    private String actionType; // CLEAR, TURBID

    @Builder
    public CustomAction(Long userId, String actionName, String actionType) {
        this.userId = userId;
        this.actionName = actionName;
        this.actionType = actionType;
    }
}
