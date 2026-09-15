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
@Table(name = "world_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorldItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false, length = 30)
    private String worldLevelCode; // 소속 세계 레벨 코드

    @Column(nullable = false, unique = true, length = 50)
    private String itemCode; // BASIC_PEBBLE, LOTUS_LEAF 등

    @Column(nullable = false, length = 100)
    private String itemNameKey; // 번역 키

    @Column(nullable = false, length = 30)
    private String itemType; // STONE, PLANT, LIGHT, DECORATION 등

    @Column(nullable = false, length = 30)
    private String slotType; // BACKGROUND, PLANT, STONE, LIGHT, DECORATION (장착 슬롯 정보)

    @Column(nullable = false)
    private Integer sortOrder; // 정렬 순서

    @Column(nullable = false, length = 1)
    private String useYn; // 사용 여부 (Y/N)

    @Builder
    public WorldItem(String worldLevelCode, String itemCode, String itemNameKey, String itemType, String slotType, Integer sortOrder, String useYn) {
        this.worldLevelCode = worldLevelCode;
        this.itemCode = itemCode;
        this.itemNameKey = itemNameKey;
        this.itemType = itemType;
        this.slotType = slotType;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
    }
}
