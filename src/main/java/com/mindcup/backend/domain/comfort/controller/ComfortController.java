package com.mindcup.backend.domain.comfort.controller;

import com.mindcup.backend.domain.comfort.dto.ComfortMessageTemplateResponse;
import com.mindcup.backend.domain.comfort.entity.ComfortMessage;
import com.mindcup.backend.domain.comfort.service.ComfortService;
import com.mindcup.backend.global.response.ApiResponse;
import com.mindcup.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Comfort API", description = "한 방울 위로 메시지 발송, 읽음 및 붓기 제어 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comfort")
public class ComfortController {

    private final ComfortService comfortService;

    @Operation(summary = "위로 템플릿 다국어 문구 리스트 조회")
    @GetMapping("/templates")
    public ApiResponse<List<ComfortMessageTemplateResponse>> getTemplates() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<ComfortMessageTemplateResponse> response = comfortService.getTemplates(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "친구에게 한 방울 전송")
    @PostMapping("/messages")
    public ApiResponse<Void> sendComfortMessage(@RequestBody SendComfortMessageDto dto) {
        Long senderId = SecurityUtil.getCurrentUserId();
        comfortService.sendComfortMessage(senderId, dto.getReceiverUserId(), dto.getTemplateId());
        return ApiResponse.success();
    }

    @Operation(summary = "내가 받은 한 방울 목록 조회")
    @GetMapping("/messages/received")
    public ApiResponse<List<ComfortMessage>> getReceivedMessages() {
        Long receiverId = SecurityUtil.getCurrentUserId();
        List<ComfortMessage> response = comfortService.getReceivedMessages(receiverId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "받은 한 방울 읽음 처리")
    @PatchMapping("/messages/{comfortId}/read")
    public ApiResponse<Void> readMessage(@PathVariable Long comfortId) {
        Long receiverId = SecurityUtil.getCurrentUserId();
        comfortService.readComfortMessage(receiverId, comfortId);
        return ApiResponse.success();
    }

    @Operation(summary = "받은 한 방울 내 컵에 붓기 (일일 한도 초과 시 409)")
    @PostMapping("/messages/{comfortId}/pour")
    public ApiResponse<Void> pourMessage(@PathVariable Long comfortId) {
        Long receiverId = SecurityUtil.getCurrentUserId();
        comfortService.pourComfortMessage(receiverId, comfortId);
        return ApiResponse.success();
    }

    @Getter
    @NoArgsConstructor
    public static class SendComfortMessageDto {
        private Long receiverUserId;
        private Long templateId;
    }
}
