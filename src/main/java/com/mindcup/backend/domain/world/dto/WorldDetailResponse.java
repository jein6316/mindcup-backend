package com.mindcup.backend.domain.world.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WorldDetailResponse {
    private String unlockedWorldLevel;
    private String displayWorldLevel;
    private Double averageClarityScore;
    private String nextWorldLevel;
    private Integer nextRequiredClarityScore;
}
