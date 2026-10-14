package com.aube.eats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class AubeEatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AubeEatsApplication.class, args);
    }
}