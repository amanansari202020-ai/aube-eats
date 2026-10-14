package com.aube.eats.controller;

import com.aube.eats.dto.ReviewRequest;
import com.aube.eats.dto.ReviewResponse;
import com.aube.eats.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menu/{menuItemId}/reviews")
public class ReviewController {
    private final ReviewService reviews;
    public ReviewController(ReviewService reviews) { this.reviews = reviews; }

    @GetMapping public Page<ReviewResponse> list(@PathVariable String menuItemId, @RequestParam(defaultValue = "0") int page) {
        return reviews.list(menuItemId, PageRequest.of(Math.max(page, 0), 20));
    }

    @PostMapping public ReviewResponse create(@PathVariable String menuItemId, @Valid @RequestBody ReviewRequest request, Authentication authentication) {
        return reviews.create(authentication.getName(), menuItemId, request);
    }
}