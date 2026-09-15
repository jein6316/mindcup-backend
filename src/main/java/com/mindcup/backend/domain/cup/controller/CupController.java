package com.mindcup.backend.domain.cup.controller;

import com.mindcup.backend.domain.cup.dto.CheckSurveyRequest;
import com.mindcup.backend.domain.cup.dto.CustomActionRequest;
import com.mindcup.backend.domain.cup.dto.CustomActionResponse;
import com.mindcup.backend.domain.cup.dto.DailyCupResponse;
import com.mindcup.backend.domain.cup.dto.RecordRequest;
import com.mindcup.backend.domain.cup.dto.RecordResponse;
import com.mindcup.backend.domain.cup.dto.WeeklyStatsResponse;
import com.mindcup.backend.domain.cup.service.CupService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cup API", description = "마음컵 관리, 설문 조사, 기록 및 통계 API")
@RestController
@RequestMapping("/api/v1/cups")
@RequiredArgsConstructor
public class CupController {

    private final CupService cupService;

    @Operation(summary = "오늘의 마음컵 상태 조회 (미생성 시 자동 생성)")
    @GetMapping("/today")
    public ApiResponse<DailyCupResponse> getOrCreateTodayCup() {
        Long userId = SecurityUtil.getCurrentUserId();
        DailyCupResponse response = cupService.getOrCreateTodayCup(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "오늘의 마음 체크 설문 제출 (하루 1회)")
    @PostMapping("/survey")
    public ApiResponse<RecordResponse> submitCheckSurvey(@Valid @RequestBody CheckSurveyRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        RecordResponse response = cupService.submitCheckSurvey(userId, request.getPollutionScore());
        return ApiResponse.success(response);
    }

    @Operation(summary = "일상 기록 등록 (맑음/흐림)")
    @PostMapping("/records")
    public ApiResponse<RecordResponse> addRecord(@Valid @RequestBody RecordRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        RecordResponse response = cupService.addRecord(userId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "최근 7일 마음 변화 통계 조회")
    @GetMapping("/statistics/weekly")
    public ApiResponse<WeeklyStatsResponse> getWeeklyStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        WeeklyStatsResponse response = cupService.getWeeklyStats(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "나만의 커스텀 행동 목록 조회")
    @GetMapping("/custom-actions")
    public ApiResponse<List<CustomActionResponse>> getCustomActions() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<CustomActionResponse> response = cupService.getCustomActions(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "나만의 커스텀 행동 등록")
    @PostMapping("/custom-actions")
    public ApiResponse<CustomActionResponse> createCustomAction(@Valid @RequestBody CustomActionRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        CustomActionResponse response = cupService.createCustomAction(userId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "나만의 커스텀 행동 삭제")
    @DeleteMapping("/custom-actions/{actionId}")
    public ApiResponse<Void> deleteCustomAction(@PathVariable Long actionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        cupService.deleteCustomAction(userId, actionId);
        return ApiResponse.success();
    }
}
