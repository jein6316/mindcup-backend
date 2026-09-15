package com.mindcup.backend.domain.report.controller;

import com.mindcup.backend.domain.report.dto.WeeklyReportResponse;
import com.mindcup.backend.domain.report.entity.WeeklyReport;
import com.mindcup.backend.domain.report.service.WeeklyReportService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Weekly Report API", description = "주간 마음 흐름 리포트 조회 및 생성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports/weekly")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @Operation(summary = "최신 주간 리포트 단건 조회 (없을 시 온디맨드 즉시 생성)")
    @GetMapping("/latest")
    public ApiResponse<WeeklyReportResponse> getLatestWeeklyReport() {
        Long userId = SecurityUtil.getCurrentUserId();
        WeeklyReportResponse response = weeklyReportService.getLatestWeeklyReport(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "현재일 기준 주간 리포트 수동 생성/재생성")
    @PostMapping("/generate")
    public ApiResponse<Void> generateWeeklyReport() {
        Long userId = SecurityUtil.getCurrentUserId();
        weeklyReportService.generateWeeklyReport(userId);
        return ApiResponse.success();
    }
}
