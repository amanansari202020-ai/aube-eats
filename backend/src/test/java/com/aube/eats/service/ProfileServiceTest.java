package com.aube.eats.service;

import com.aube.eats.domain.Entities.Address;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.dto.AddressRequest;
import com.aube.eats.dto.ProfileRequest;
import com.aube.eats.repository.AddressRepository;
import com.aube.eats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock UserRepository users;
    @Mock AddressRepository addresses;
    private ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(users, addresses);
    }

    @Test
    void updateProfileTrimsNameAndNormalizesEmail() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Old Name");
        user.setEmail("old@example.com");
        when(users.findByEmailIgnoreCase("old@example.com")).thenReturn(Optional.of(user));
        when(users.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());

        var result = service.updateProfile("old@example.com", new ProfileRequest(" Avery ", " NEW@Example.com "));

        assertEquals("Avery", result.name());
        assertEquals("new@example.com", result.email());
        assertEquals(user.getId(), result.id());
    }

    @Test
    void addAddressTrimsFieldsAndUppercasesCountryCode() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(users.findByEmailIgnoreCase("avery@example.com")).thenReturn(Optional.of(user));
        when(addresses.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AddressRequest request = new AddressRequest(" Home ", " 18 Baker Street ", " Flat 2 ", " London ", " SW1A 1AA ", " gb ");

        var response = service.addAddress("avery@example.com", request);

        assertEquals("Home", response.label());
        assertEquals("18 Baker Street", response.street());
        assertEquals("London", response.city());
        assertEquals("SW1A 1AA", response.postalCode());
        assertEquals("GB", response.countryCode());
        verify(addresses).save(any(Address.class));
    }
}