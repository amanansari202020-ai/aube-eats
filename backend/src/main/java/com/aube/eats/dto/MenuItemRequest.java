package com.aube.eats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MenuItemRequest(@NotBlank @Size(max = 64) String id, @NotNull Long categoryId, @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 600) String description, @NotNull @PositiveOrZero BigDecimal price, @Size(max = 500) String imageUrl, boolean vegetarian) {}