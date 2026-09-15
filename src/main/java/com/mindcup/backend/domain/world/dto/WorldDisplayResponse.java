package com.mindcup.backend.domain.world.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WorldDisplayResponse {
    private String unlockedWorldLevel;
    private String displayWorldLevel;
    private String nextWorldLevel;
    private Double averageClarityScore;
    private Integer nextRequiredClarityScore;
    private Integer pollutionScore;
    private Integer clarityScore;
    private String mindCupLevel; // CLEAR, SLIGHTLY_CLOUDY, CLOUDY, VERY_CLOUDY
    private String creatureState; // ACTIVE, SLOW, RESTING, HIDDEN
    private String messageKey; // creature.active 등 i18n 메시지 키
    private List<CreatureDto> creatures;
    private List<ItemDto> equippedItems;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreatureDto {
        private String creatureCode;
        private String creatureName;
        private String creatureType;
        private String defaultVisibleYn;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ItemDto {
        private String itemCode;
        private String itemName;
        private String itemType;
        private String slotType;
    }
}
