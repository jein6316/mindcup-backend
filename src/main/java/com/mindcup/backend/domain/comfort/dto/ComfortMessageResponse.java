package com.mindcup.backend.domain.comfort.dto;

import com.mindcup.backend.domain.comfort.entity.ComfortMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ComfortMessageResponse {
    private Long comfortId;
    private Long senderUserId;
    private String senderNickname;
    private String content; // 번역 완료된 템플릿 문구
    private String readYn;
    private String pouredYn;
    private LocalDateTime createdAt;
    private LocalDateTime pouredAt;

    public static ComfortMessageResponse of(ComfortMessage msg, String senderNickname, boolean isKorean) {
        String content = isKorean ? msg.getTemplate().getContentKo() : msg.getTemplate().getContentEn();
        return ComfortMessageResponse.builder()
                .comfortId(msg.getComfortId())
                .senderUserId(msg.getSenderUserId())
                .senderNickname(senderNickname)
                .content(content)
                .readYn(msg.getReadYn())
                .pouredYn(msg.getPouredYn())
                .createdAt(msg.getCreatedAt())
                .pouredAt(msg.getPouredAt())
                .build();
    }
}
