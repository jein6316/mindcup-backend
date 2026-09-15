package com.mindcup.backend.domain.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DailyMissionResponse {
    private Long missionId;
    private String missionCode;
    private String title;       // 다국어 처리된 타이틀
    private String description; // 다국어 처리된 설명
    private Integer effectScore;
    private Boolean completedYn;
}
