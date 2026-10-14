package com.aube.eats.dto;

import java.math.BigDecimal;
public record MenuItemDto(String id, String name, String description, BigDecimal price, String category, String imageUrl, boolean vegetarian) {}