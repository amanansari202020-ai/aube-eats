package com.aube.eats.service;

import com.aube.eats.domain.Entities.Category;
import com.aube.eats.domain.Entities.Coupon;
import com.aube.eats.domain.Entities.CustomerOrder;
import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.dto.CategoryRequest;
import com.aube.eats.dto.CouponRequest;
import com.aube.eats.dto.DashboardSummary;
import com.aube.eats.dto.MenuItemDto;
import com.aube.eats.dto.MenuItemRequest;
import com.aube.eats.dto.OrderResponse;
import com.aube.eats.dto.UserSummary;
import com.aube.eats.mapper.MenuMapper;
import com.aube.eats.repository.CategoryRepository;
import com.aube.eats.repository.CouponRepository;
import com.aube.eats.repository.CustomerOrderRepository;
import com.aube.eats.repository.MenuItemRepository;
import com.aube.eats.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final CustomerOrderRepository orders;
    private final UserRepository users;
    private final MenuItemRepository menuItems;
    private final CategoryRepository categories;
    private final CouponRepository coupons;
    private final MenuMapper menuMapper;

    public AdminService(CustomerOrderRepository orders, UserRepository users, MenuItemRepository menuItems, CategoryRepository categories, CouponRepository coupons, MenuMapper menuMapper) {
        this.orders = orders; this.users = users; this.menuItems = menuItems; this.categories = categories; this.coupons = coupons; this.menuMapper = menuMapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummary dashboard() {
        var allOrders = orders.findAll();
        BigDecimal revenue = allOrders.stream().filter(order -> order.getStatus() == com.aube.eats.domain.OrderStatus.DELIVERED)
            .map(CustomerOrder::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();
        for (int day = 6; day >= 0; day--) revenueByDay.put(today.minusDays(day).toString(), BigDecimal.ZERO);
        allOrders.stream().filter(order -> order.getStatus() == com.aube.eats.domain.OrderStatus.DELIVERED)
            .filter(order -> !order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().isBefore(today.minusDays(6)))
            .forEach(order -> {
                String day = order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().toString();
                revenueByDay.computeIfPresent(day, (ignored, total) -> total.add(order.getTotal()));
            });
        Map<String, Long> breakdown = allOrders.stream().collect(Collectors.groupingBy(order -> order.getStatus().name(), Collectors.counting()));
        return new DashboardSummary(allOrders.size(), users.count(), menuItems.countByActiveTrue(), revenue, revenueByDay, breakdown);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> orders(Pageable pageable) { return orders.findAll(pageable).map(this::toResponse); }

    @Transactional(readOnly = true)
    public Page<UserSummary> users(Pageable pageable) {
        return users.findAll(pageable).map(user -> new UserSummary(user.getId(), user.getName(), user.getEmail(), user.isEnabled(), user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())));
    }

    @Transactional
    public UserSummary setUserEnabled(java.util.UUID id, boolean enabled) {
        User user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setEnabled(enabled);
        return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.isEnabled(), user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()));
    }

    @Transactional(readOnly = true)
    public Page<MenuItemDto> menu(Pageable pageable) { return menuItems.findAll(pageable).map(menuMapper::toDto); }

    @Transactional
    public MenuItemDto saveMenuItem(String id, MenuItemRequest request) {
        if (!id.equals(request.id())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path and menu item ids must match");
        Category category = categories.findById(request.categoryId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        MenuItem item = menuItems.findById(id).orElseGet(MenuItem::new);
        item.setId(id); item.setCategory(category); item.setName(request.name().trim()); item.setDescription(request.description().trim());
        item.setPrice(request.price()); item.setImageUrl(request.imageUrl()); item.setVegetarian(request.vegetarian()); item.setActive(true);
        return menuMapper.toDto(menuItems.save(item));
    }

    @Transactional
    public void archiveMenuItem(String id) {
        MenuItem item = menuItems.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));
        item.setActive(false);
    }

    @Transactional(readOnly = true)
    public Page<Category> categories(Pageable pageable) { return categories.findAll(pageable); }

    @Transactional
    public Category saveCategory(Long id, CategoryRequest request) {
        Category category = id == null ? new Category() : categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setName(request.name().trim()); category.setDescription(request.description()); category.setActive(true);
        return categories.save(category);
    }

    @Transactional
    public void archiveCategory(Long id) {
        Category category = categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setActive(false);
    }

    @Transactional(readOnly = true)
    public Page<Coupon> coupons(Pageable pageable) { return coupons.findAll(pageable); }

    @Transactional
    public Coupon saveCoupon(Long id, CouponRequest request) {
        String code = request.code().trim().toUpperCase();
        coupons.findByCodeIgnoreCase(code).filter(existing -> id == null || !existing.getId().equals(id))
            .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Coupon code already exists"); });
        Coupon coupon = id == null ? new Coupon() : coupons.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        coupon.setCode(code); coupon.setDiscountPercent(request.discountPercent()); coupon.setExpiresAt(request.expiresAt()); coupon.setActive(true);
        return coupons.save(coupon);
    }

    @Transactional
    public void archiveCoupon(Long id) {
        Coupon coupon = coupons.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        coupon.setActive(false);
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getStatus(), order.getTotal(), order.getDeliveryAddress(), order.getCreatedAt(),
            order.getItems().stream().map(item -> new OrderResponse.Line(item.getItemName(), item.getQuantity(), item.getUnitPrice())).toList());
    }
}