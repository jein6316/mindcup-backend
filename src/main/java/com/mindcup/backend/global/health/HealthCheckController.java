package com.mindcup.backend.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health Check API", description = "서버 헬스 체크 및 무중단 핑(Ping) 수신용 API")
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @Operation(summary = "서버 상태 및 Liveness 헬스 체크")
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "appName", "MindCup",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
