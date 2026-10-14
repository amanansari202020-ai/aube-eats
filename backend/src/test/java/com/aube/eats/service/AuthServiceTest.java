package com.aube.eats.service;

import com.aube.eats.domain.Entities.Role;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.dto.RegisterRequest;
import com.aube.eats.dto.TokenResponse;
import com.aube.eats.repository.RoleRepository;
import com.aube.eats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenService tokens;
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, roles, passwordEncoder, tokens);
    }

    @Test
    void registerNormalizesProfileAndAssignsUserRole() {
        RegisterRequest request = new RegisterRequest(" Avery ", " Avery@Example.com ", "long-password");
        Role role = new Role("ROLE_USER");
        TokenResponse token = new TokenResponse("access", "refresh", "Bearer", 900);
        when(users.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());
        when(roles.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.issue(any(User.class))).thenReturn(token);

        assertEquals(token, service.register(request));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(users).save(savedUser.capture());
        assertEquals("Avery", savedUser.getValue().getName());
        assertEquals("avery@example.com", savedUser.getValue().getEmail());
        assertEquals("hashed-password", savedUser.getValue().getPasswordHash());
        assertEquals(java.util.Set.of(role), savedUser.getValue().getRoles());
    }

    @Test
    void registerRejectsExistingEmail() {
        when(users.findByEmailIgnoreCase("avery@example.com")).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.register(new RegisterRequest("Avery", "avery@example.com", "long-password")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}