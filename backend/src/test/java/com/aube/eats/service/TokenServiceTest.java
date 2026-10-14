package com.aube.eats.service;

import com.aube.eats.domain.Entities.User;
import com.aube.eats.repository.UserRepository;
import com.aube.eats.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock JwtService jwtService;
    @Mock UserRepository users;
    private TokenService service;

    @BeforeEach
    void setUp() {
        service = new TokenService(jwtService, users);
    }

    @Test
    void issuePersistsHashedRefreshTokenAndReturnsAccessToken() {
        User user = new User();
        Instant beforeIssue = Instant.now();
        when(jwtService.createAccessToken(user)).thenReturn("access-token");
        when(jwtService.getAccessTokenSeconds()).thenReturn(900L);

        var response = service.issue(user);

        assertEquals("access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.expiresInSeconds());
        assertNotEquals(response.refreshToken(), user.getRefreshTokenHash());
        assertEquals(64, user.getRefreshTokenHash().length());
        assertTrue(user.getRefreshExpiresAt().isAfter(beforeIssue.plusSeconds(30L * 24 * 60 * 60 - 2)));
        verify(users).save(user);
    }

    @Test
    void rotateRejectsExpiredRefreshToken() {
        User user = new User();
        user.setRefreshExpiresAt(Instant.now().minusSeconds(1));
        when(users.findByRefreshTokenHash(anyString())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.rotate("expired-token"));
    }
}