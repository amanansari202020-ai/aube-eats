package com.aube.eats.repository;

import com.aube.eats.domain.Entities.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    Page<CustomerOrder> findByUserId(UUID userId, Pageable pageable);
    boolean existsByUser_IdAndStatusAndItems_MenuItem_Id(UUID userId, com.aube.eats.domain.OrderStatus status, String menuItemId);
}