package com.mindcup.backend.domain.cup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckSurveyRequest {

    @NotNull(message = "초기 탁도 점수는 필수입니다.")
    @Min(value = 0, message = "탁도 점수는 0 이상이어야 합니다.")
    @Max(value = 100, message = "탁도 점수는 100 이하이어야 합니다.")
    private Integer pollutionScore;
}
