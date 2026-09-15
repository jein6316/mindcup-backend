package com.mindcup.backend.domain.world.entity;

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
@Table(name = "world_levels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorldLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long worldLevelId;

    @Column(nullable = false, unique = true, length = 30)
    private String worldLevelCode; // SMALL_CUP, LARGE_CUP, AQUARIUM, POND 등

    @Column(nullable = false, length = 100)
    private String worldLevelNameKey; // 번역 키

    @Column(nullable = false)
    private Integer requiredAverageClarityScore; // 해금에 필요한 최근 7일 평균 맑음도

    @Column(nullable = false)
    private Integer sortOrder; // 정렬 순서

    @Column(nullable = false, length = 1)
    private String useYn; // 사용 여부 (Y/N)

    @Builder
    public WorldLevel(String worldLevelCode, String worldLevelNameKey, Integer requiredAverageClarityScore, Integer sortOrder, String useYn) {
        this.worldLevelCode = worldLevelCode;
        this.worldLevelNameKey = worldLevelNameKey;
        this.requiredAverageClarityScore = requiredAverageClarityScore;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
    }
}
