package com.aube.eats.controller;

import com.aube.eats.dto.CouponPreview;
import com.aube.eats.repository.CouponRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {
    private final CouponRepository coupons;
    public CouponController(CouponRepository coupons) { this.coupons = coupons; }

    @GetMapping("/{code}")
    public CouponPreview validate(@PathVariable String code) {
        var coupon = coupons.findByCodeIgnoreCase(code.trim()).filter(value -> value.isActive() && value.getExpiresAt().isAfter(Instant.now()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon is invalid or expired"));
        return new CouponPreview(coupon.getCode(), coupon.getDiscountPercent());
    }
}