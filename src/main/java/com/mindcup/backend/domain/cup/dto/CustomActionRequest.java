package com.mindcup.backend.domain.cup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomActionRequest {

    @NotBlank(message = "행동 명칭은 필수입니다.")
    private String actionName;

    @NotBlank(message = "행동 타입은 필수입니다.")
    @Pattern(regexp = "CLEAR|TURBID", message = "행동 타입은 CLEAR 또는 TURBID만 가능합니다.")
    private String actionType;
}
