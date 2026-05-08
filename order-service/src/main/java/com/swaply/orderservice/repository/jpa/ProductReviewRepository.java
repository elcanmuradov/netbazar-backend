package com.swaply.orderservice.repository.jpa;

import com.swaply.orderservice.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
