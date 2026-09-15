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
@Table(name = "ecosystem_creatures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcosystemCreature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creatureId;

    @Column(nullable = false, length = 30)
    private String worldLevelCode; // 소속 세계 레벨 코드

    @Column(nullable = false, unique = true, length = 50)
    private String creatureCode; // SILVER_MINNOW, GUPPY 등

    @Column(nullable = false, length = 100)
    private String creatureNameKey; // 번역 키

    @Column(nullable = false, length = 30)
    private String creatureType; // FISH, AMPHIBIAN, PLANT, SHELL, DECORATION 등

    @Column(nullable = false, length = 1)
    private String defaultVisibleYn; // 해금 시 자동 배치 및 노출 여부 (Y/N)

    @Column(nullable = false)
    private Integer sortOrder; // 정렬 순서

    @Column(nullable = false, length = 1)
    private String useYn; // 사용 여부 (Y/N)

    @Builder
    public EcosystemCreature(String worldLevelCode, String creatureCode, String creatureNameKey, String creatureType, String defaultVisibleYn, Integer sortOrder, String useYn) {
        this.worldLevelCode = worldLevelCode;
        this.creatureCode = creatureCode;
        this.creatureNameKey = creatureNameKey;
        this.creatureType = creatureType;
        this.defaultVisibleYn = defaultVisibleYn;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
    }
}
