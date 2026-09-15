package com.mindcup.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageUpdateRequest {

    @NotBlank(message = "언어 설정은 필수입니다.")
    @Pattern(regexp = "KO|EN", message = "언어 설정은 KO 또는 EN만 가능합니다.")
    private String languageSetting;
}
