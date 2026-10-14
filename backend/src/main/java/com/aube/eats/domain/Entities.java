package com.aube.eats.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Entities {
    private Entities() {}

    @Entity @Table(name = "roles") @Getter @Setter @NoArgsConstructor
    public static class Role {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false, unique = true, length = 32) private String name;
        public Role(String name) { this.name = name; }
    }

    @Entity @Table(name = "app_users", indexes = @Index(name = "idx_users_email", columnList = "email", unique = true)) @Getter @Setter @NoArgsConstructor
    public static class User {
        @Id @UuidGenerator private UUID id;
        @Column(nullable = false, length = 120) private String name;
        @Column(nullable = false, unique = true, length = 190) private String email;
        @Column(name = "password_hash", nullable = false) private String passwordHash;
        @Column(nullable = false) private boolean enabled = true;
        @ManyToMany(fetch = FetchType.EAGER) @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id")) private Set<Role> roles = new HashSet<>();
        @Column(name = "refresh_token_hash", length = 64) private String refreshTokenHash;
        @Column(name = "refresh_expires_at") private Instant refreshExpiresAt;
        @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    }

    @Entity @Table(name = "addresses") @Getter @Setter @NoArgsConstructor
    public static class Address {
        @Id @UuidGenerator private UUID id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") @JsonIgnore private User user;
        @Column(nullable = false, length = 80) private String label;
        @Column(nullable = false, length = 180) private String street;
        @Column(length = 100) private String apartment;
        @Column(nullable = false, length = 80) private String city;
        @Column(nullable = false, length = 16) private String postalCode;
        @Column(nullable = false, length = 2) private String countryCode = "US";
    }

    @Entity @Table(name = "categories") @Getter @Setter @NoArgsConstructor
    public static class Category {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false, unique = true, length = 80) private String name;
        @Column(length = 240) private String description;
        @Column(nullable = false) private boolean active = true;
    }

    @Entity @Table(name = "menu_items", indexes = @Index(name = "idx_menu_active_category", columnList = "active, category_id")) @Getter @Setter @NoArgsConstructor
    public static class MenuItem {
        @Id @Column(length = 64) private String id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id") private Category category;
        @Column(nullable = false, length = 120) private String name;
        @Column(nullable = false, length = 600) private String description;
        @Column(nullable = false, precision = 9, scale = 2) private BigDecimal price;
        @Column(length = 500) private String imageUrl;
        @Column(nullable = false) private boolean vegetarian;
        @Column(nullable = false) private boolean active = true;
    }

    @Entity @Table(name = "carts") @Getter @Setter @NoArgsConstructor
    public static class Cart {
        @Id @UuidGenerator private UUID id;
        @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", unique = true) private User user;
        @Column(nullable = false) private Instant updatedAt = Instant.now();
    }

    @Entity @Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "menu_item_id"})) @Getter @Setter @NoArgsConstructor
    public static class CartItem {
        @Id @UuidGenerator private UUID id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cart_id") @JsonIgnore private Cart cart;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "menu_item_id") private MenuItem menuItem;
        @Column(nullable = false) private int quantity;
    }

    @Entity @Table(name = "customer_orders", indexes = @Index(name = "idx_orders_user_created", columnList = "user_id, created_at")) @Getter @Setter @NoArgsConstructor
    public static class CustomerOrder {
        @Id @UuidGenerator private UUID id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
        @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private OrderStatus status = OrderStatus.PLACED;
        @Column(nullable = false, precision = 10, scale = 2) private BigDecimal total;
        @Column(name = "delivery_address", nullable = false, length = 300) private String deliveryAddress;
        @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private Set<OrderItem> items = new HashSet<>();
        public void addItem(OrderItem item) { items.add(item); item.setOrder(this); }
    }

    @Entity @Table(name = "order_items") @Getter @Setter @NoArgsConstructor
    public static class OrderItem {
        @Id @UuidGenerator private UUID id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id") @JsonIgnore private CustomerOrder order;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "menu_item_id") private MenuItem menuItem;
        @Column(nullable = false, length = 120) private String itemName;
        @Column(nullable = false) private int quantity;
        @Column(nullable = false, precision = 9, scale = 2) private BigDecimal unitPrice;
    }

    @Entity @Table(name = "payments") @Getter @Setter @NoArgsConstructor
    public static class Payment {
        @Id @UuidGenerator private UUID id;
        @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id", unique = true) private CustomerOrder order;
        @Column(nullable = false, length = 24) private String method;
        @Column(nullable = false, length = 24) private String status = "PENDING";
        @Column(name = "provider_payment_id", length = 120) private String providerPaymentId;
        @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    }

    @Entity @Table(name = "coupons") @Getter @Setter @NoArgsConstructor
    public static class Coupon {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false, unique = true, length = 48) private String code;
        @Column(nullable = false, precision = 5, scale = 2) private BigDecimal discountPercent;
        @Column(nullable = false) private Instant expiresAt;
        @Column(nullable = false) private boolean active = true;
    }

    @Entity @Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "menu_item_id"})) @Getter @Setter @NoArgsConstructor
    public static class Review {
        @Id @UuidGenerator private UUID id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
        @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "menu_item_id") private MenuItem menuItem;
        @Column(nullable = false) private int rating;
        @Column(length = 1000) private String comment;
        @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    }
}