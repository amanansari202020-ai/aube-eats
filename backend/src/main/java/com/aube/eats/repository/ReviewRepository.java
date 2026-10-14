package com.aube.eats.repository;

import com.aube.eats.domain.Entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByUserIdAndMenuItemId(UUID userId, String menuItemId);
    boolean existsByUserIdAndMenuItemIdAndIdNot(UUID userId, String menuItemId, UUID reviewId);
    Page<Review> findByMenuItemIdOrderByCreatedAtDesc(String menuItemId, Pageable pageable);
}