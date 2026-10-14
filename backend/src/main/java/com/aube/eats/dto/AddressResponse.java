package com.aube.eats.dto;

import java.util.UUID;

public record AddressResponse(UUID id, String label, String street, String apartment, String city, String postalCode, String countryCode) {}