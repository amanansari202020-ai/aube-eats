package com.aube.eats.dto;

import com.aube.eats.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, UUID userId, OrderStatus status, BigDecimal total, String deliveryAddress, Instant createdAt, List<Line> items) {
    public record Line(String name, int quantity, BigDecimal unitPrice) {}
}