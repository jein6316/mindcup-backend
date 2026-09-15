package com.mindcup.backend.domain.friend.controller;

import com.mindcup.backend.domain.friend.dto.FriendMindStatusResponse;
import com.mindcup.backend.domain.friend.entity.Friend;
import com.mindcup.backend.domain.friend.service.FriendService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Friend API", description = "친구 맺기, 차단 및 공개범위 마스킹 마음상태 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "내 친구 목록 조회 (마음상태 마스킹 반영)")
    @GetMapping
    public ApiResponse<List<FriendMindStatusResponse>> getFriends() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<FriendMindStatusResponse> response = friendService.getFriendsMindStatusList(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "친구 코드로 친구 신청 전송")
    @PostMapping("/requests/by-code")
    public ApiResponse<Void> sendFriendRequest(@RequestBody FriendRequestDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        friendService.sendFriendRequest(userId, dto.getTargetFriendCode());
        return ApiResponse.success();
    }

    @Operation(summary = "나에게 온 친구 신청 대기 목록 조회")
    @GetMapping("/requests/received")
    public ApiResponse<List<Friend>> getReceivedRequests() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Friend> requests = friendService.getReceivedPendingRequests(userId);
        return ApiResponse.success(requests);
    }

    @Operation(summary = "친구 신청 수락")
    @PostMapping("/requests/{requestId}/accept")
    public ApiResponse<Void> acceptRequest(@PathVariable Long requestId) {
        Long userId = SecurityUtil.getCurrentUserId();
        friendService.acceptFriendRequest(userId, requestId);
        return ApiResponse.success();
    }

    @Operation(summary = "친구 신청 거절")
    @PostMapping("/requests/{requestId}/reject")
    public ApiResponse<Void> rejectRequest(@PathVariable Long requestId) {
        Long userId = SecurityUtil.getCurrentUserId();
        friendService.rejectFriendRequest(userId, requestId);
        return ApiResponse.success();
    }

    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId) {
        Long userId = SecurityUtil.getCurrentUserId();
        friendService.deleteFriend(userId, friendId);
        return ApiResponse.success();
    }

    @Operation(summary = "친구 차단")
    @PostMapping("/{friendId}/block")
    public ApiResponse<Void> blockFriend(@PathVariable Long friendId) {
        Long userId = SecurityUtil.getCurrentUserId();
        friendService.blockFriend(userId, friendId);
        return ApiResponse.success();
    }

    @Operation(summary = "특정 친구의 마음 상태 단일 조회 (마스킹 적용)")
    @GetMapping("/{friendUserId}/mind-status")
    public ApiResponse<FriendMindStatusResponse> getSingleFriendMindStatus(@PathVariable Long friendUserId) {
        Long userId = SecurityUtil.getCurrentUserId();
        FriendMindStatusResponse response = friendService.getSingleFriendMindStatus(userId, friendUserId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 친구 설정 공개 범위 변경")
    @PostMapping("/visibility")
    public ApiResponse<Void> updateVisibilitySetting(@RequestBody VisibilitySettingDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        friendService.updateVisibilitySetting(userId, dto.getVisibilityLevel());
        return ApiResponse.success();
    }

    @Operation(summary = "내 친구 설정 공개 범위 조회")
    @GetMapping("/visibility")
    public ApiResponse<String> getVisibilitySetting() {
        Long userId = SecurityUtil.getCurrentUserId();
        String level = friendService.getMyVisibilitySetting(userId);
        return ApiResponse.success(level);
    }

    @Getter
    @NoArgsConstructor
    public static class FriendRequestDto {
        private String targetFriendCode;
    }

    @Getter
    @NoArgsConstructor
    public static class VisibilitySettingDto {
        private String visibilityLevel;
    }
}
