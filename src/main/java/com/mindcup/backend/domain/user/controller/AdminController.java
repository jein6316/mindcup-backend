package com.mindcup.backend.domain.user.controller;

import com.mindcup.backend.domain.user.dto.UserResponse;
import com.mindcup.backend.domain.user.entity.Role;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import com.mindcup.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Admin API", description = "관리자 전용 대시보드 및 회원 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @Operation(summary = "전체 회원 목록 및 대시보드 요약 정보 조회")
    @GetMapping("/users")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        long totalUsers = users.size();
        long adminCount = users.stream().filter(u -> u.getRole() == Role.ROLE_ADMIN).count();
        long regularUserCount = totalUsers - adminCount;

        Map<String, Object> result = new HashMap<>();
        result.put("users", userResponses);
        result.put("totalUsers", totalUsers);
        result.put("adminCount", adminCount);
        result.put("regularUserCount", regularUserCount);

        return ApiResponse.success(result);
    }

    @Operation(summary = "특정 회원 권한 변경 (ROLE_USER <-> ROLE_ADMIN)")
    @PatchMapping("/users/{userId}/role")
    @Transactional
    public ApiResponse<UserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        String roleStr = body.get("role");
        if ("ROLE_ADMIN".equalsIgnoreCase(roleStr)) {
            user.updateRole(Role.ROLE_ADMIN);
        } else {
            user.updateRole(Role.ROLE_USER);
        }

        return ApiResponse.success(UserResponse.from(user));
    }
}
