package com.aube.eats.config;

import com.aube.eats.domain.Entities.Role;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.repository.RoleRepository;
import com.aube.eats.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository users, RoleRepository roles, PasswordEncoder passwords,
        @Value("${app.bootstrap.admin-email:}") String email,
        @Value("${app.bootstrap.admin-password:}") String password) {
        this.users = users; this.roles = roles; this.passwords = passwords; this.email = email; this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 12 || password.length() > 72) throw new IllegalStateException("Configure both admin bootstrap credentials; password must be 12-72 characters");
        if (users.findByEmailIgnoreCase(email).isPresent()) return;
        Role admin = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));
        User user = new User();
        user.setName("Aube administrator");
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwords.encode(password));
        user.setRoles(Set.of(admin));
        users.save(user);
    }
}