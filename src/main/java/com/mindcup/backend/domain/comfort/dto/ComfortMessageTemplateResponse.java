package com.mindcup.backend.domain.comfort.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ComfortMessageTemplateResponse {
    private Long templateId;
    private String content;
}
