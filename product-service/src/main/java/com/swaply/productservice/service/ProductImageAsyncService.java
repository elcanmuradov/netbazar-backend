package com.swaply.productservice.service;

import com.swaply.productservice.client.MediaClient;
import com.swaply.productservice.document.ProductDocument;
import com.swaply.productservice.entity.Product;
import com.swaply.productservice.entity.ProductImage;
import com.swaply.productservice.repository.elastic.ElasticProductRepository;
import com.swaply.productservice.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageAsyncService {

    private final MediaClient mediaClient;
    private final ProductRepository productRepository;
    private final ElasticProductRepository elasticProductRepository;

    @Async("productTaskExecutor")
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void uploadProductImagesAsync(List<MultipartFile> files, UUID productId) {
        try {
            log.info("Starting background image upload for product: {}", productId);
            var response = mediaClient.uploadMultipleFiles(files);

            if (response == null || response.getData() == null) {
                log.warn("Media upload returned empty response for product: {}", productId);
                return;
            }

            Product product = productRepository.findById(productId).orElseThrow();
            List<Map<String, String>> uploadResults = response.getData();
            List<ProductImage> list = product.getImages() != null ? product.getImages() : new ArrayList<>();

            if (uploadResults.isEmpty()) {
                log.warn("Media upload returned zero files for product: {}", productId);
                return;
            }

            AtomicInteger i = new AtomicInteger();
            uploadResults.forEach(res -> {
                ProductImage image = new ProductImage();
                image.setImageUrl(res.get("url"));
                image.setPublicId(res.get("publicId"));
                image.setDisplayOrder(i.getAndIncrement());
                image.setIsMain(i.get() == 1);
                image.setProduct(product);
                list.add(image);
            });

            product.setImages(list);
            productRepository.save(product);
            syncToElastic(product);
            log.info("Product images updated successfully for product: {}, imageCount: {}", productId, list.size());
        } catch (Exception e) {
            log.error("Error uploading images for product {}", productId, e);
        }
    }

    @Async("productTaskExecutor")
    public void syncProductToElasticAsync(UUID productId) {
        try {
            Product product = productRepository.findById(productId).orElseThrow();
            syncToElastic(product);
        } catch (Exception e) {
            log.error("Error syncing product {} to ElasticSearch", productId, e);
        }
    }

    private void syncToElastic(Product product) {
        try {
            List<String> orderedImageUrls = product.getImages() == null
                ? new ArrayList<>()
                : product.getImages().stream()
                    .sorted(Comparator.comparing(
                        img -> img.getDisplayOrder() == null ? Integer.MAX_VALUE : img.getDisplayOrder()
                    ))
                    .map(ProductImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ProductDocument doc = ProductDocument.builder()
                    .id(product.getId().toString())
                    .userId(product.getUserId() != null ? product.getUserId().toString() : null)
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .price(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0)
                    .category(product.getCategory())
                    .city(product.getCity())
                    .status(product.getStatus())
                    .isNew(product.getIsNew())
                    .isDelivery(product.getIsDelivery())
                    .createdAt(product.getCreatedAt() != null
                        ? product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : null)
                    .imageUrls(orderedImageUrls)
                    .build();

            elasticProductRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to sync product to ElasticSearch: {}", e.getMessage());
        }
    }
}
