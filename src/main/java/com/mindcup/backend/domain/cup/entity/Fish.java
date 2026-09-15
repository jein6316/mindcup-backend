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
@Table(name = "fishes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fishId;

    @Column(nullable = false)
    private String fishNameKo;

    @Column(nullable = false)
    private String fishNameEn;

    @Column(nullable = false)
    private String status; // CLEAR, SLIGHTLY_CLOUDY, CLOUDY, VERY_CLOUDY

    @Column(length = 500)
    private String descriptionKo;

    @Column(length = 500)
    private String descriptionEn;

    @Builder
    public Fish(String fishNameKo, String fishNameEn, String status, String descriptionKo, String descriptionEn) {
        this.fishNameKo = fishNameKo;
        this.fishNameEn = fishNameEn;
        this.status = status;
        this.descriptionKo = descriptionKo;
        this.descriptionEn = descriptionEn;
    }
}
