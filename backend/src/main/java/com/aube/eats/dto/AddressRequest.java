package com.aube.eats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(@NotBlank @Size(max = 80) String label, @NotBlank @Size(max = 180) String street,
    @Size(max = 100) String apartment, @NotBlank @Size(max = 80) String city,
    @NotBlank @Size(max = 16) String postalCode, @NotBlank @Size(min = 2, max = 2) String countryCode) {}