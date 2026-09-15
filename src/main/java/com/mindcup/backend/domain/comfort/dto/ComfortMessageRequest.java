package com.mindcup.backend.domain.comfort.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComfortMessageRequest {

    @NotNull(message = "수신인 유저 ID는 필수입니다.")
    private Long receiverUserId;

    @NotNull(message = "위로 문구 템플릿 ID는 필수입니다.")
    private Long templateId;
}
