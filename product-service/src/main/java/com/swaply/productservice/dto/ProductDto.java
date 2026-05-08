package com.swaply.productservice.dto;

import com.swaply.productservice.utils.enums.City;
import com.swaply.productservice.utils.enums.ProductCategory;
import com.swaply.productservice.utils.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID userId;

    private String title;

    private String description;

    private Double price;
    private Double discountedPrice;
    private Integer discountPercentage;
    private Boolean isNew;

    private ProductCategory category;

    private Boolean isDelivery;

    private Date createdAt;

    private Date updatedAt;

    private boolean isUpdated;

    private LocalDate deletedAt;

    private Long viewers;

    private City city;

    private ProductStatus status;

    private List<String> imageUrls;



}
