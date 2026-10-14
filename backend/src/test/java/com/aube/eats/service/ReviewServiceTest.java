package com.aube.eats.service;

import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.domain.Entities.Review;
import com.aube.eats.domain.Entities.User;
import com.aube.eats.domain.OrderStatus;
import com.aube.eats.dto.ReviewRequest;
import com.aube.eats.repository.CustomerOrderRepository;
import com.aube.eats.repository.MenuItemRepository;
import com.aube.eats.repository.ReviewRepository;
import com.aube.eats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock ReviewRepository reviews;
    @Mock UserRepository users;
    @Mock MenuItemRepository menuItems;
    @Mock CustomerOrderRepository orders;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviews, users, menuItems, orders);
    }

    @Test
    void createAllowsDeliveredCustomerToReviewActiveItem() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Avery");
        MenuItem item = new MenuItem();
        item.setId("sesame");
        when(users.findByEmailIgnoreCase("avery@example.com")).thenReturn(Optional.of(user));
        when(menuItems.findById("sesame")).thenReturn(Optional.of(item));
        when(orders.existsByUser_IdAndStatusAndItems_MenuItem_Id(user.getId(), OrderStatus.DELIVERED, "sesame"))
            .thenReturn(true);
        when(reviews.existsByUserIdAndMenuItemId(user.getId(), "sesame")).thenReturn(false);
        when(reviews.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create("avery@example.com", "sesame", new ReviewRequest(5, "Fresh and delicious"));

        assertEquals("Avery", response.customerName());
        assertEquals(5, response.rating());
        assertEquals("Fresh and delicious", response.comment());
    }

    @Test
    void createRejectsCustomerWithoutDeliveredItem() {
        User user = new User();
        user.setId(UUID.randomUUID());
        MenuItem item = new MenuItem();
        when(users.findByEmailIgnoreCase("avery@example.com")).thenReturn(Optional.of(user));
        when(menuItems.findById("sesame")).thenReturn(Optional.of(item));
        when(orders.existsByUser_IdAndStatusAndItems_MenuItem_Id(user.getId(), OrderStatus.DELIVERED, "sesame"))
            .thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create("avery@example.com", "sesame", new ReviewRequest(4, "Nice")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(reviews, never()).save(any(Review.class));
    }
}