package com.aube.eats.controller;

import com.aube.eats.domain.Entities.Category;
import com.aube.eats.domain.Entities.Coupon;
import com.aube.eats.dto.*;
import com.aube.eats.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService admin;
    public AdminController(AdminService admin) { this.admin = admin; }

    @GetMapping("/dashboard") public DashboardSummary dashboard() { return admin.dashboard(); }
    @GetMapping("/orders") public Page<OrderResponse> orders(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return admin.orders(page(page, size, "createdAt")); }
    @GetMapping("/users") public Page<UserSummary> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return admin.users(page(page, size, "name")); }
    @PatchMapping("/users/{id}/enabled") public UserSummary setEnabled(@PathVariable UUID id, @RequestParam boolean enabled) { return admin.setUserEnabled(id, enabled); }
    @GetMapping("/menu") public Page<MenuItemDto> menu(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) { return admin.menu(page(page, size, "name")); }
    @PostMapping("/menu") public MenuItemDto createMenu(@Valid @RequestBody MenuItemRequest request) { return admin.saveMenuItem(request.id(), request); }
    @PutMapping("/menu/{id}") public MenuItemDto updateMenu(@PathVariable String id, @Valid @RequestBody MenuItemRequest request) { return admin.saveMenuItem(id, request); }
    @DeleteMapping("/menu/{id}") public void archiveMenu(@PathVariable String id) { admin.archiveMenuItem(id); }
    @GetMapping("/categories") public Page<Category> categories(@RequestParam(defaultValue = "0") int page) { return admin.categories(page(page, 50, "name")); }
    @PostMapping("/categories") public Category createCategory(@Valid @RequestBody CategoryRequest request) { return admin.saveCategory(null, request); }
    @PutMapping("/categories/{id}") public Category updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) { return admin.saveCategory(id, request); }
    @DeleteMapping("/categories/{id}") public void archiveCategory(@PathVariable Long id) { admin.archiveCategory(id); }
    @GetMapping("/coupons") public Page<Coupon> coupons(@RequestParam(defaultValue = "0") int page) { return admin.coupons(page(page, 50, "code")); }
    @PostMapping("/coupons") public Coupon createCoupon(@Valid @RequestBody CouponRequest request) { return admin.saveCoupon(null, request); }
    @PutMapping("/coupons/{id}") public Coupon updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) { return admin.saveCoupon(id, request); }
    @DeleteMapping("/coupons/{id}") public void archiveCoupon(@PathVariable Long id) { admin.archiveCoupon(id); }

    private Pageable page(int page, int size, String sort) { return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sort).ascending()); }
}