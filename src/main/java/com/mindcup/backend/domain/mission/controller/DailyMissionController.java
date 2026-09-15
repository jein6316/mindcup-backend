package com.mindcup.backend.domain.mission.controller;

import com.mindcup.backend.domain.mission.dto.DailyMissionResponse;
import com.mindcup.backend.domain.mission.service.DailyMissionService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Daily Mission API", description = "오늘의 작은 맑은물 미션 조회 및 완료 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/daily-missions")
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;

    @Operation(summary = "오늘의 작은 맑은물 미션 목록 조회")
    @GetMapping("/today")
    public ApiResponse<List<DailyMissionResponse>> getTodayMissions() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<DailyMissionResponse> response = dailyMissionService.getTodayMissions(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "미션 완료 처리")
    @PostMapping("/{missionId}/complete")
    public ApiResponse<Void> completeMission(@PathVariable Long missionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        dailyMissionService.completeMission(userId, missionId);
        return ApiResponse.success();
    }
}
