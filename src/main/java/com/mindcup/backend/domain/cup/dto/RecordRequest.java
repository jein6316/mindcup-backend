package com.mindcup.backend.domain.cup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordRequest {

    @NotBlank(message = "기록 타입은 필수입니다.")
    @Pattern(regexp = "CLEAR|TURBID", message = "기록 타입은 CLEAR(맑음) 또는 TURBID(흐림)만 가능합니다.")
    private String recordType;

    @NotBlank(message = "행동 또는 감정 명칭은 필수입니다.")
    private String actionName;

    @NotNull(message = "영향 강도는 필수입니다.")
    @Min(value = 1, message = "강도는 최소 1 이상이어야 합니다.")
    @Max(value = 5, message = "강도는 최대 5 이하여야 합니다.")
    private Integer intensity;

    private String memo;
}
