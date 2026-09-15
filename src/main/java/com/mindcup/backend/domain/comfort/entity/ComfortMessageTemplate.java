package com.mindcup.backend.domain.comfort.entity;

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
@Table(name = "comfort_message_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComfortMessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(nullable = false, length = 255)
    private String contentKo;

    @Column(nullable = false, length = 255)
    private String contentEn;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ComfortMessageTemplate(String contentKo, String contentEn) {
        this.contentKo = contentKo;
        this.contentEn = contentEn;
        this.createdAt = LocalDateTime.now();
    }
}
