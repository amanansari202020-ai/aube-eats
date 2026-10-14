package com.aube.eats.service;

import com.aube.eats.domain.Entities.Coupon;
import com.aube.eats.domain.Entities.CustomerOrder;
import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.domain.Entities.OrderItem;
import com.aube.eats.domain.Entities.Payment;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.domain.OrderStatus;
import com.aube.eats.dto.CreateOrderRequest;
import com.aube.eats.dto.OrderResponse;
import com.aube.eats.repository.CouponRepository;
import com.aube.eats.repository.CustomerOrderRepository;
import com.aube.eats.repository.MenuItemRepository;
import com.aube.eats.repository.PaymentRepository;
import com.aube.eats.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3.50");
    private final CustomerOrderRepository orders;
    private final MenuItemRepository menuItems;
    private final PaymentRepository payments;
    private final UserRepository users;
    private final CouponRepository coupons;
    private final ApplicationEventPublisher events;

    public OrderService(CustomerOrderRepository orders, MenuItemRepository menuItems, PaymentRepository payments,
        UserRepository users, CouponRepository coupons, ApplicationEventPublisher events) {
        this.orders = orders;
        this.menuItems = menuItems;
        this.payments = payments;
        this.users = users;
        this.coupons = coupons;
        this.events = events;
    }

    @Transactional
    public OrderResponse create(String email, CreateOrderRequest request) {
        String method = request.paymentMethod().trim().toUpperCase();
        if (!method.equals("COD")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment method");
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setDeliveryAddress(request.deliveryAddress().trim());
        order.setStatus(OrderStatus.PLACED);
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.LineItem line : request.items()) {
            MenuItem item = menuItems.findById(line.menuItemId()).filter(MenuItem::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "A menu item is unavailable"));
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(item);
            orderItem.setItemName(item.getName());
            orderItem.setQuantity(line.quantity());
            orderItem.setUnitPrice(item.getPrice());
            order.addItem(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Coupon coupon = coupons.findByCodeIgnoreCase(request.couponCode().trim()).filter(Coupon::isActive)
                .filter(value -> value.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon is invalid or expired"));
            BigDecimal discount = coupon.getDiscountPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            total = total.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
        }
        total = total.add(DELIVERY_FEE);
        order.setTotal(total);
        CustomerOrder saved = orders.save(order);
        Payment payment = new Payment();
        payment.setOrder(saved);
        payment.setMethod(method);
        payment.setStatus(method.equals("COD") ? "AWAITING_CASH" : "PENDING");
        payments.save(payment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> mine(String email, Pageable pageable) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return orders.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse reorder(String email, UUID id) {
        CustomerOrder previous = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!previous.getUser().getEmail().equalsIgnoreCase(email)) throw new AccessDeniedException("This order belongs to another customer");
        CreateOrderRequest request = new CreateOrderRequest(previous.getItems().stream()
            .map(item -> new CreateOrderRequest.LineItem(item.getMenuItem().getId(), item.getQuantity())).toList(),
            previous.getDeliveryAddress(), "COD", null);
        return create(email, request);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String email, UUID id, boolean admin) {
        CustomerOrder order = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!admin && !order.getUser().getEmail().equalsIgnoreCase(email)) throw new AccessDeniedException("This order belongs to another customer");
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus next) {
        CustomerOrder order = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getStatus().canTransitionTo(next)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid order status transition");
        order.setStatus(next);
        events.publishEvent(new OrderStatusChangedEvent(order.getId(), next));
        return toResponse(order);
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getStatus(), order.getTotal(), order.getDeliveryAddress(), order.getCreatedAt(),
            order.getItems().stream().map(item -> new OrderResponse.Line(item.getItemName(), item.getQuantity(), item.getUnitPrice())).toList());
    }
}