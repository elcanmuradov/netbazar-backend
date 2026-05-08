package com.swaply.productservice.repository.jpa;

import com.swaply.productservice.entity.Product;
import com.swaply.productservice.utils.enums.ProductCategory;
import com.swaply.productservice.utils.enums.ProductStatus;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @EntityGraph(attributePaths = {"images"})
    Optional<Product> getProductById(UUID productId);

    long countByUserIdAndStatus(UUID userId, ProductStatus status);

    @EntityGraph(attributePaths = {"images"})
    Page<Product> getProductsByStatusAndUserId(ProductStatus status, UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    Page<Product> getProductsByStatus(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    Page<Product> getProductByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    Page<Product> getProductsByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    List<Product> getProductsByIsReported(Boolean isReported);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status = :status WHERE p.createdAt < :thresholdDate AND p.status != :status")
    int expireOldProducts(@Param("thresholdDate") LocalDateTime thresholdDate,
                          @Param("status") ProductStatus status);
}
