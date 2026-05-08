package com.swaply.productservice.dto.create;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Başlık gerekli")
    private String title;

    private UUID userId;

    private String description;

    @NotNull(message = "Fiyat gerekli")
    @DecimalMin(value = "0.01", message = "Fiyat 0'dan büyük olmalı")
    private BigDecimal price;

    @Builder.Default
    private Boolean isNew = false;

    @NotNull(message = "Kategori gerekli")
    private Integer categoryId;

    @Builder.Default
    private Boolean isDelivery = false;

    @NotNull(message = "Şehir gerekli")
    private Integer cityId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageRequest {
        private String imageUrl;
        private String publicId;
    }
}