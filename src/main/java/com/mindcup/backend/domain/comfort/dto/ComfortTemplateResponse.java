package com.mindcup.backend.domain.comfort.dto;

import com.mindcup.backend.domain.comfort.entity.ComfortMessageTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComfortTemplateResponse {
    private Long templateId;
    private String content;

    public static ComfortTemplateResponse of(ComfortMessageTemplate template, boolean isKorean) {
        String content = isKorean ? template.getContentKo() : template.getContentEn();
        return new ComfortTemplateResponse(template.getTemplateId(), content);
    }
}
