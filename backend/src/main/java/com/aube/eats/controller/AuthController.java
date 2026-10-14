package com.aube.eats.controller;

import com.aube.eats.dto.AuthRequest;
import com.aube.eats.dto.RefreshRequest;
import com.aube.eats.dto.RegisterRequest;
import com.aube.eats.dto.TokenResponse;
import com.aube.eats.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register") public TokenResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }
    @PostMapping("/login") public TokenResponse login(@Valid @RequestBody AuthRequest request) { return authService.login(request); }
    @PostMapping("/refresh") public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) { return authService.refresh(request); }
}