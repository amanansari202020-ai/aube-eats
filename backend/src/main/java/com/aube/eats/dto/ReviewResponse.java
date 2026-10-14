package com.aube.eats.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(UUID id, String customerName, int rating, String comment, Instant createdAt) {}