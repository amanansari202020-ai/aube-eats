package com.aube.eats.service;

import com.aube.eats.domain.Entities.Address;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.dto.AddressRequest;
import com.aube.eats.dto.AddressResponse;
import com.aube.eats.dto.ProfileRequest;
import com.aube.eats.dto.UserSummary;
import com.aube.eats.repository.AddressRepository;
import com.aube.eats.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    private final UserRepository users;
    private final AddressRepository addresses;

    public ProfileService(UserRepository users, AddressRepository addresses) { this.users = users; this.addresses = addresses; }

    @Transactional(readOnly = true)
    public UserSummary getProfile(String email) { return summary(user(email)); }

    @Transactional
    public UserSummary updateProfile(String email, ProfileRequest request) {
        User user = user(email);
        String normalizedEmail = request.email().trim().toLowerCase();
        users.findByEmailIgnoreCase(normalizedEmail).filter(existing -> !existing.getId().equals(user.getId()))
            .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use"); });
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        return summary(user);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> addresses(String email) {
        User user = user(email);
        return addresses.findByUserIdOrderByLabel(user.getId()).stream().map(this::addressResponse).toList();
    }

    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        Address address = new Address();
        address.setUser(user(email));
        return saveAddress(address, request);
    }

    @Transactional
    public AddressResponse updateAddress(String email, UUID id, AddressRequest request) {
        User user = user(email);
        Address address = addresses.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        return saveAddress(address, request);
    }

    @Transactional
    public void deleteAddress(String email, UUID id) {
        User user = user(email);
        Address address = addresses.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        addresses.delete(address);
    }

    private AddressResponse saveAddress(Address address, AddressRequest request) {
        address.setLabel(request.label().trim());
        address.setStreet(request.street().trim());
        address.setApartment(request.apartment());
        address.setCity(request.city().trim());
        address.setPostalCode(request.postalCode().trim());
        address.setCountryCode(request.countryCode().trim().toUpperCase());
        return addressResponse(addresses.save(address));
    }

    private User user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)); }
    private UserSummary summary(User user) { return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.isEnabled(), user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())); }
    private AddressResponse addressResponse(Address address) { return new AddressResponse(address.getId(), address.getLabel(), address.getStreet(), address.getApartment(), address.getCity(), address.getPostalCode(), address.getCountryCode()); }
}