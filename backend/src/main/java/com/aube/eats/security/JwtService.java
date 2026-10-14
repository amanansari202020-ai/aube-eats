package com.aube.eats.security;

import com.aube.eats.domain.Entities.Role;
import com.aube.eats.domain.Entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTokenSeconds;

    public JwtService(@Value("${app.jwt.secret-base64}") String secret, @Value("${app.jwt.access-token-seconds:900}") long accessTokenSeconds) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenSeconds = accessTokenSeconds;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return Jwts.builder().subject(user.getEmail()).claim("subId", user.getId().toString()).claim("roles", roles)
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessTokenSeconds))).signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessTokenSeconds() { return accessTokenSeconds; }
}