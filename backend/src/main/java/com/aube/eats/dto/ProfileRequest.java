package com.aube.eats.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Email String email) {}