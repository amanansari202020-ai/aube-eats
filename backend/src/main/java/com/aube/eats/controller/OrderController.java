package com.aube.eats.controller;

import com.aube.eats.domain.OrderStatus;
import com.aube.eats.dto.CreateOrderRequest;
import com.aube.eats.dto.OrderResponse;
import com.aube.eats.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/orders") public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        return orderService.create(authentication.getName(), request);
    }

    @GetMapping("/orders/mine") public Page<OrderResponse> mine(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return orderService.mine(authentication.getName(), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)));
    }

    @PostMapping("/orders/{id}/reorder") public OrderResponse reorder(@PathVariable UUID id, Authentication authentication) {
        return orderService.reorder(authentication.getName(), id);
    }

    @GetMapping("/orders/{id}") public OrderResponse get(@PathVariable UUID id, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return orderService.get(authentication.getName(), id, admin);
    }

    @PatchMapping("/admin/orders/{id}/status") @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateStatus(@PathVariable UUID id, @RequestParam OrderStatus status) { return orderService.updateStatus(id, status); }
}