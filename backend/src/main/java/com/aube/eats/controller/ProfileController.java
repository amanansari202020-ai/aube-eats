package com.aube.eats.controller;

import com.aube.eats.dto.AddressRequest;
import com.aube.eats.dto.AddressResponse;
import com.aube.eats.dto.ProfileRequest;
import com.aube.eats.dto.UserSummary;
import com.aube.eats.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
public class ProfileController {
    private final ProfileService profiles;
    public ProfileController(ProfileService profiles) { this.profiles = profiles; }

    @GetMapping public UserSummary get(Authentication authentication) { return profiles.getProfile(authentication.getName()); }
    @PutMapping public UserSummary update(@Valid @RequestBody ProfileRequest request, Authentication authentication) { return profiles.updateProfile(authentication.getName(), request); }
    @GetMapping("/addresses") public List<AddressResponse> addresses(Authentication authentication) { return profiles.addresses(authentication.getName()); }
    @PostMapping("/addresses") public AddressResponse addAddress(@Valid @RequestBody AddressRequest request, Authentication authentication) { return profiles.addAddress(authentication.getName(), request); }
    @PutMapping("/addresses/{id}") public AddressResponse updateAddress(@PathVariable UUID id, @Valid @RequestBody AddressRequest request, Authentication authentication) { return profiles.updateAddress(authentication.getName(), id, request); }
    @DeleteMapping("/addresses/{id}") public void deleteAddress(@PathVariable UUID id, Authentication authentication) { profiles.deleteAddress(authentication.getName(), id); }
}