package com.mindcup.backend.domain.user.controller;

import com.mindcup.backend.domain.user.dto.GoogleLoginRequest;
import com.mindcup.backend.domain.user.dto.LoginRequest;
import com.mindcup.backend.domain.user.dto.SignupRequest;
import com.mindcup.backend.domain.user.dto.TokenResponse;
import com.mindcup.backend.domain.user.dto.UserResponse;
import com.mindcup.backend.domain.user.service.AuthService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth API", description = "회원가입, 로그인 및 인증 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 회원가입")
    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Google 소셜 로그인")
    @PostMapping("/google")
    public ApiResponse<TokenResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        TokenResponse response = authService.googleLogin(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "JWT 토큰 재발급 (Refresh)")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        TokenResponse response = authService.refresh(refreshToken);
        return ApiResponse.success(response);
    }

    @Operation(summary = "로그아웃 (토큰 만료)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.logout(userId);
        return ApiResponse.success();
    }
}
