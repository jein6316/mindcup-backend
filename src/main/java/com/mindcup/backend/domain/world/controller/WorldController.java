package com.mindcup.backend.domain.world.controller;

import com.mindcup.backend.domain.world.dto.WorldDetailResponse;
import com.mindcup.backend.domain.world.dto.WorldDisplayResponse;
import com.mindcup.backend.domain.world.entity.EcosystemCreature;
import com.mindcup.backend.domain.world.entity.UserWorld;
import com.mindcup.backend.domain.world.entity.UserWorldItem;
import com.mindcup.backend.domain.world.entity.WorldItem;
import com.mindcup.backend.domain.world.entity.WorldLevel;
import com.mindcup.backend.domain.world.repository.WorldLevelRepository;
import com.mindcup.backend.domain.world.service.WorldService;
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

@Tag(name = "World API", description = "마음의 세계 성장, 생태계 생물 도감 및 꾸미기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/worlds")
public class WorldController {

    private final WorldService worldService;
    private final WorldLevelRepository worldLevelRepository;

    @Operation(summary = "내 현재 세계 통계 정보 조회")
    @GetMapping("/me")
    public ApiResponse<WorldDetailResponse> getMyWorldDetail() {
        Long userId = SecurityUtil.getCurrentUserId();
        WorldDetailResponse response = worldService.getMyWorldDetail(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "최근 7일 평균 clarityScore 기준으로 worldLevel 수동 재계산")
    @PostMapping("/me/recalculate")
    public ApiResponse<UserWorld> recalculateWorldLevel() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserWorld userWorld = worldService.recalculateWorldLevel(userId);
        return ApiResponse.success(userWorld);
    }

    @Operation(summary = "전체 세계 레벨 마스터 가이드라인 목록 조회")
    @GetMapping("/levels")
    public ApiResponse<List<WorldLevel>> getAllWorldLevels() {
        List<WorldLevel> levels = worldLevelRepository.findAll();
        return ApiResponse.success(levels);
    }

    @Operation(summary = "전체 도감용 생물 목록 조회")
    @GetMapping("/creatures")
    public ApiResponse<List<EcosystemCreature>> getAllCreatures() {
        List<EcosystemCreature> creatures = worldService.getAllCreatures();
        return ApiResponse.success(creatures);
    }

    @Operation(summary = "내가 수집/해금 완료한 생물 목록 조회")
    @GetMapping("/creatures/me")
    public ApiResponse<List<EcosystemCreature>> getMyAcquiredCreatures() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<EcosystemCreature> creatures = worldService.getMyAcquiredCreatures(userId);
        return ApiResponse.success(creatures);
    }

    @Operation(summary = "전체 꾸미기 조경 아이템 목록 조회")
    @GetMapping("/items")
    public ApiResponse<List<WorldItem>> getAllWorldItems() {
        List<WorldItem> items = worldService.getAllWorldItems();
        return ApiResponse.success(items);
    }

    @Operation(summary = "내가 획득/보유한 조경 아이템 목록 조회")
    @GetMapping("/items/me")
    public ApiResponse<List<UserWorldItem>> getMyWorldItems() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<UserWorldItem> items = worldService.getMyWorldItems(userId);
        return ApiResponse.success(items);
    }

    @Operation(summary = "조경 아이템 장착 (슬롯당 1개 제한 자동 해제)")
    @PostMapping("/items/{itemId}/equip")
    public ApiResponse<Void> equipItem(@PathVariable Long itemId) {
        Long userId = SecurityUtil.getCurrentUserId();
        worldService.equipItem(userId, itemId);
        return ApiResponse.success();
    }

    @Operation(summary = "조경 아이템 장착 해제")
    @PostMapping("/items/{itemId}/unequip")
    public ApiResponse<Void> unequipItem(@PathVariable Long itemId) {
        Long userId = SecurityUtil.getCurrentUserId();
        worldService.unequipItem(userId, itemId);
        return ApiResponse.success();
    }

    @Operation(summary = "홈 화면 노출용 통합 디스플레이 데이터 일괄 조회")
    @GetMapping("/me/display")
    public ApiResponse<WorldDisplayResponse> getHomeDisplayData() {
        Long userId = SecurityUtil.getCurrentUserId();
        WorldDisplayResponse response = worldService.getHomeDisplayData(userId);
        return ApiResponse.success(response);
    }
}
