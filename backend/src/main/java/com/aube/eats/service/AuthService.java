package com.aube.eats.service;

import com.aube.eats.domain.Entities.Role;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.dto.AuthRequest;
import com.aube.eats.dto.RefreshRequest;
import com.aube.eats.dto.RegisterRequest;
import com.aube.eats.dto.TokenResponse;
import com.aube.eats.repository.RoleRepository;
import com.aube.eats.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (users.findByEmailIgnoreCase(request.email()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        Role userRole = roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(userRole));
        return tokens.issue(users.save(user));
    }

    @Transactional
    public TokenResponse login(AuthRequest request) {
        User user = users.findByEmailIgnoreCase(request.email()).filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
        return tokens.issue(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        try { return tokens.rotate(request.refreshToken()); }
        catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired"); }
    }
}