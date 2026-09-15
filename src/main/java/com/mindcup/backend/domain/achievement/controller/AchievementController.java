package com.mindcup.backend.domain.achievement.controller;

import com.mindcup.backend.domain.achievement.dto.AchievementResponse;
import com.mindcup.backend.domain.achievement.service.AchievementService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Achievement API", description = "업적 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "전체 업적 목록 조회 (내 달성 여부 포함)")
    @GetMapping
    public ApiResponse<List<AchievementResponse>> getAchievements() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<AchievementResponse> response = achievementService.getAchievements(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내가 획득한 업적 목록 조회")
    @GetMapping("/me")
    public ApiResponse<List<AchievementResponse>> getMyAchievements() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<AchievementResponse> response = achievementService.getAchievements(userId).stream()
                .filter(AchievementResponse::getAchievedYn)
                .toList();
        return ApiResponse.success(response);
    }
}
