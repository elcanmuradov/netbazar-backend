package com.swaply.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UpdateProductRequest {
    private String title;

    private UUID userId;

    private String description;

    private BigDecimal price;

    private Boolean isNew;

    @NotNull(message = "Kategori gerekli")
    private Integer categoryId;

    private Boolean isDelivery;

    private Integer cityId;

    @Builder.Default
    private List<ImageRequest> images = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageRequest {
        private String imageUrl;
        private String publicId;
    }
}
