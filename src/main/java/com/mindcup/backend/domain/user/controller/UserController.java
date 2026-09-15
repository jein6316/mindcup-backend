package com.mindcup.backend.domain.user.controller;

import com.mindcup.backend.domain.user.dto.LanguageUpdateRequest;
import com.mindcup.backend.domain.user.dto.UserResponse;
import com.mindcup.backend.domain.user.service.UserService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 정보 조회 및 설정 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserResponse response = userService.getUserProfile(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "사용자 언어 설정 변경")
    @PutMapping("/me/language")
    public ApiResponse<UserResponse> updateLanguage(@Valid @RequestBody LanguageUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserResponse response = userService.updateLanguage(userId, request.getLanguageSetting());
        return ApiResponse.success(response);
    }
}
