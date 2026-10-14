package com.aube.eats.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotEmpty List<@Valid LineItem> items,
    @NotBlank String deliveryAddress,
    @NotBlank String paymentMethod,
    String couponCode
) {
    public record LineItem(@NotBlank String menuItemId, @Min(1) @Max(20) int quantity) {}
}