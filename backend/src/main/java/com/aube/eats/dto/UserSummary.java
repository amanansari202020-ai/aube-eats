package com.aube.eats.dto;

import java.util.Set;
import java.util.UUID;

public record UserSummary(UUID id, String name, String email, boolean enabled, Set<String> roles) {}