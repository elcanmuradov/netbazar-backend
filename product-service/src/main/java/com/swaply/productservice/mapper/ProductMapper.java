package com.swaply.productservice.mapper;

import com.swaply.productservice.document.ProductDocument;
import com.swaply.productservice.dto.ProductDto;
import com.swaply.productservice.dto.UpdateProductRequest;
import com.swaply.productservice.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        componentModel = "spring"
)
public interface ProductMapper {
    @Mapping(target = "isUpdated", ignore = true)
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "imageUrls", expression = "java(mapImages(product))")
    ProductDto toDto(Product product);

    default List<String> mapImages(Product product) {
        if (product.getImages() == null) return null;
        return product.getImages().stream()
                .sorted(Comparator.comparing(img -> img.getDisplayOrder() == null ? Integer.MAX_VALUE : img.getDisplayOrder()))
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());
    }



}
