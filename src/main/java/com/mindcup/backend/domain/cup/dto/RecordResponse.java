package com.mindcup.backend.domain.cup.dto;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RecordResponse {
    private Long recordId;
    private String recordType;
    private String actionName;
    private Integer intensity;
    private String memo;
    private LocalDateTime createdAt;
    private Integer currentPollutionScore;
    private Integer currentClarityScore;
    private String currentStatus;

    public static RecordResponse of(MindRecord record, DailyCup dailyCup) {
        return RecordResponse.builder()
                .recordId(record.getRecordId())
                .recordType(record.getRecordType())
                .actionName(record.getActionName())
                .intensity(record.getIntensity())
                .memo(record.getMemo())
                .createdAt(record.getCreatedAt())
                .currentPollutionScore(dailyCup.getPollutionScore())
                .currentClarityScore(dailyCup.getClarityScore())
                .currentStatus(dailyCup.getCupStatus())
                .build();
    }
}
