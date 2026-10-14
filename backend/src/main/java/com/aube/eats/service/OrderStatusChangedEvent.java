package com.aube.eats.service;

import com.aube.eats.domain.OrderStatus;

import java.util.UUID;

public record OrderStatusChangedEvent(UUID orderId, OrderStatus status) {}