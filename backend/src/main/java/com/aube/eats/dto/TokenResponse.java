package com.aube.eats.dto;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}