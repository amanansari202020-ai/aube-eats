package com.aube.eats.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(@NotBlank String code, @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal discountPercent, @NotNull @Future Instant expiresAt) {}