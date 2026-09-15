package com.mindcup.backend.domain.cup.dto;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.Fish;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class DailyCupResponse {
    private Long cupId;
    private LocalDate date;
    private Integer pollutionScore;
    private Integer clarityScore;
    private String activeWorldLevel;
    private String status;
    private FishResponse fish;

    public static DailyCupResponse of(DailyCup dailyCup, Fish fish, boolean isKorean) {
        return DailyCupResponse.builder()
                .cupId(dailyCup.getCupId())
                .date(dailyCup.getRecordDate())
                .pollutionScore(dailyCup.getPollutionScore())
                .clarityScore(dailyCup.getClarityScore())
                .activeWorldLevel(dailyCup.getActiveWorldLevel())
                .status(dailyCup.getCupStatus())
                .fish(fish != null ? FishResponse.from(fish, isKorean) : null)
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class FishResponse {
        private String name;
        private String status;
        private String description;

        public static FishResponse from(Fish fish, boolean isKorean) {
            String name = isKorean ? fish.getFishNameKo() : fish.getFishNameEn();
            String desc = isKorean ? fish.getDescriptionKo() : fish.getDescriptionEn();
            return new FishResponse(name, fish.getStatus(), desc);
        }
    }
}
