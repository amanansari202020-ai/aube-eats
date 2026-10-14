package com.aube.eats.service;

import com.aube.eats.dto.CreateOrderRequest;
import com.aube.eats.domain.Entities.CustomerOrder;
import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.repository.CustomerOrderRepository;
import com.aube.eats.repository.CouponRepository;
import com.aube.eats.repository.MenuItemRepository;
import com.aube.eats.repository.PaymentRepository;
import com.aube.eats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock CustomerOrderRepository orders;
    @Mock MenuItemRepository menuItems;
    @Mock PaymentRepository payments;
    @Mock UserRepository users;
    @Mock CouponRepository coupons;
    @Mock ApplicationEventPublisher events;
    private OrderService service;

    @BeforeEach
    void setUp() { service = new OrderService(orders, menuItems, payments, users, coupons, events); }

    @Test
    void rejectsUnsupportedPaymentBeforeLoadingCustomerOrPricingItems() {
        var request = new CreateOrderRequest(List.of(new CreateOrderRequest.LineItem("sesame", 1)), "18 Baker Street", "CARD", null);
        assertThrows(ResponseStatusException.class, () -> service.create("avery@example.com", request));
        verifyNoInteractions(orders, menuItems, payments, users, coupons, events);
    }

    @Test
    void rejectsOnlinePaymentUntilPaymentVerificationIsImplemented() {
        var request = new CreateOrderRequest(List.of(new CreateOrderRequest.LineItem("sesame", 1)), "18 Baker Street", "RAZORPAY", null);
        assertThrows(ResponseStatusException.class, () -> service.create("avery@example.com", request));
        verifyNoInteractions(orders, menuItems, payments, users, coupons, events);
    }

    @Test
    void includesDeliveryFeeInOrderTotal() {
        var user = new User();
        user.setEmail("avery@example.com");
        var item = new MenuItem();
        item.setId("sesame");
        item.setName("Sesame Bagel");
        item.setPrice(new BigDecimal("10.00"));
        when(users.findByEmailIgnoreCase("avery@example.com")).thenReturn(Optional.of(user));
        when(menuItems.findById("sesame")).thenReturn(Optional.of(item));
        when(orders.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateOrderRequest(List.of(new CreateOrderRequest.LineItem("sesame", 1)), "18 Baker Street", "COD", null);
        var response = service.create("avery@example.com", request);

        assertEquals(0, new BigDecimal("13.50").compareTo(response.total()));
    }
}