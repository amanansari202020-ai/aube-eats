package com.aube.eats.service;

import com.aube.eats.domain.Entities.User;
import com.aube.eats.dto.TokenResponse;
import com.aube.eats.repository.UserRepository;
import com.aube.eats.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenService {
    private static final long REFRESH_DAYS = 30;
    private final JwtService jwtService;
    private final UserRepository users;

    public TokenService(JwtService jwtService, UserRepository users) { this.jwtService = jwtService; this.users = users; }

    @Transactional
    public TokenResponse issue(User user) {
        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        user.setRefreshTokenHash(hash(refreshToken));
        user.setRefreshExpiresAt(Instant.now().plusSeconds(REFRESH_DAYS * 24 * 60 * 60));
        users.save(user);
        return new TokenResponse(jwtService.createAccessToken(user), refreshToken, "Bearer", jwtService.getAccessTokenSeconds());
    }

    @Transactional
    public TokenResponse rotate(String refreshToken) {
        User user = users.findByRefreshTokenHash(hash(refreshToken)).filter(candidate -> candidate.getRefreshExpiresAt() != null && candidate.getRefreshExpiresAt().isAfter(Instant.now()))
            .orElseThrow(() -> new IllegalArgumentException("Refresh token is invalid or expired"));
        return issue(user);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}