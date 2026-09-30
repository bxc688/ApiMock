package com.apimock.controller;

import com.apimock.model.dto.MockResponse;
import com.apimock.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public MockResponse<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return MockResponse.error(400, "用户名和密码不能为空");
        }

        String token = authService.login(username, password);
        if (token == null) {
            return MockResponse.error(401, "用户名或密码错误");
        }

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        return MockResponse.ok(data);
    }

    @PostMapping("/logout")
    public MockResponse<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return MockResponse.ok();
    }

    @GetMapping("/check")
    public MockResponse<Map<String, String>> check(HttpServletRequest request) {
        String token = extractToken(request);
        if (authService.validateToken(token)) {
            Map<String, String> data = new HashMap<>();
            data.put("authenticated", "true");
            return MockResponse.ok(data);
        }
        return MockResponse.error(401, "未登录或登录已过期");
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
