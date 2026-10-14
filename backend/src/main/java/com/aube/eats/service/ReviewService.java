package com.aube.eats.service;

import com.aube.eats.domain.Entities.Review;
import com.aube.eats.domain.OrderStatus;
import com.aube.eats.dto.ReviewRequest;
import com.aube.eats.dto.ReviewResponse;
import com.aube.eats.repository.CustomerOrderRepository;
import com.aube.eats.repository.MenuItemRepository;
import com.aube.eats.repository.ReviewRepository;
import com.aube.eats.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {
    private final ReviewRepository reviews;
    private final UserRepository users;
    private final MenuItemRepository menuItems;
    private final CustomerOrderRepository orders;

    public ReviewService(ReviewRepository reviews, UserRepository users, MenuItemRepository menuItems, CustomerOrderRepository orders) {
        this.reviews = reviews; this.users = users; this.menuItems = menuItems; this.orders = orders;
    }

    @Transactional
    public ReviewResponse create(String email, String menuItemId, ReviewRequest request) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var item = menuItems.findById(menuItemId).filter(value -> value.isActive()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));
        if (!orders.existsByUser_IdAndStatusAndItems_MenuItem_Id(user.getId(), OrderStatus.DELIVERED, menuItemId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers who received this item can review it");
        }
        if (reviews.existsByUserIdAndMenuItemId(user.getId(), menuItemId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this item");
        Review review = new Review();
        review.setUser(user); review.setMenuItem(item); review.setRating(request.rating()); review.setComment(request.comment());
        return response(reviews.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> list(String menuItemId, Pageable pageable) {
        return reviews.findByMenuItemIdOrderByCreatedAtDesc(menuItemId, pageable).map(this::response);
    }

    private ReviewResponse response(Review review) {
        return new ReviewResponse(review.getId(), review.getUser().getName(), review.getRating(), review.getComment(), review.getCreatedAt());
    }
}