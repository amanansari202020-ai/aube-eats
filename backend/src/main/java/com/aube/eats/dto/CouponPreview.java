package com.aube.eats.dto;

import java.math.BigDecimal;

public record CouponPreview(String code, BigDecimal discountPercent) {}