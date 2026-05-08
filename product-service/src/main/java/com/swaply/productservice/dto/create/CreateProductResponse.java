package com.swaply.productservice.dto.create;

import com.swaply.productservice.entity.ProductImage;
import com.swaply.productservice.utils.enums.City;
import com.swaply.productservice.utils.enums.ProductCategory;
import com.swaply.productservice.utils.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductResponse {
    private UUID id;

    private UUID userId;

    private String title;

    private String description;

    private BigDecimal price;

    @Builder.Default
    private Boolean isNew = false;

    private ProductCategory category;

    @Builder.Default
    private Boolean isDelivery = false;

    private City city;

    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Builder.Default
    private Long viewers = 0L;

    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
