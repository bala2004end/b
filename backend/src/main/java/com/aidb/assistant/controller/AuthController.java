package com.aidb.assistant.controller;

import com.aidb.assistant.dto.AuthRequest;
import com.aidb.assistant.dto.AuthResponse;
import com.aidb.assistant.dto.RegisterRequest;
import com.aidb.assistant.service.AuthService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication endpoints with per-IP rate limiting.
 * 10 requests per minute per IP address to prevent credential stuffing / brute-force attacks.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    private final AuthService authService;

    // Per-IP bucket cache — ConcurrentHashMap is thread-safe for concurrent reads/writes
    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    private Bucket getBucketForIp(String ipAddress) {
        return ipBuckets.computeIfAbsent(ipAddress, ip ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(
                    MAX_REQUESTS_PER_MINUTE,
                    Refill.greedy(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                ))
                .build()
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Take only the first IP if there are multiple hops
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        if (!getBucketForIp(clientIp).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many login attempts. Please try again in a minute."));
        }
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        if (!getBucketForIp(clientIp).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many registration attempts. Please try again in a minute."));
        }
        return ResponseEntity.ok(authService.register(request));
    }
}
