package com.aube.eats.repository;

import com.aube.eats.domain.Entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserIdOrderByLabel(UUID userId);
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
}