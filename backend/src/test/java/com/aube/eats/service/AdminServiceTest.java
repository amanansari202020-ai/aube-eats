package com.aube.eats.service;

import com.aube.eats.domain.Entities.Coupon;
import com.aube.eats.dto.CouponRequest;
import com.aube.eats.mapper.MenuMapper;
import com.aube.eats.repository.CategoryRepository;
import com.aube.eats.repository.CouponRepository;
import com.aube.eats.repository.CustomerOrderRepository;
import com.aube.eats.repository.MenuItemRepository;
import com.aube.eats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock CustomerOrderRepository orders;
    @Mock UserRepository users;
    @Mock MenuItemRepository menuItems;
    @Mock CategoryRepository categories;
    @Mock CouponRepository coupons;
    @Mock MenuMapper menuMapper;
    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(orders, users, menuItems, categories, coupons, menuMapper);
    }

    @Test
    void saveCouponNormalizesCodeAndActivatesCoupon() {
        CouponRequest request = new CouponRequest(" save10 ", new BigDecimal("10.00"), Instant.now().plusSeconds(3600));
        when(coupons.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.empty());
        when(coupons.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon coupon = service.saveCoupon(null, request);

        assertEquals("SAVE10", coupon.getCode());
        assertEquals(new BigDecimal("10.00"), coupon.getDiscountPercent());
        org.junit.jupiter.api.Assertions.assertTrue(coupon.isActive());
    }

    @Test
    void saveCouponRejectsDuplicateCode() {
        when(coupons.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(new Coupon()));
        CouponRequest request = new CouponRequest("save10", new BigDecimal("10.00"), Instant.now().plusSeconds(3600));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.saveCoupon(null, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}