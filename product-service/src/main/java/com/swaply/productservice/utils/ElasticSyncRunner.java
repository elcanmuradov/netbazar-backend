package com.swaply.productservice.utils;

import com.swaply.productservice.entity.Product;
import com.swaply.productservice.repository.jpa.ProductRepository;
import com.swaply.productservice.service.ProductService;
import com.swaply.productservice.utils.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSyncRunner implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    public void run(String... args) {
        log.info("Starting initial synchronization of active products to Elasticsearch...");
        try {
            List<Product> activeProducts = productRepository.getProductsByStatus(ProductStatus.ACTIVE, org.springframework.data.domain.Pageable.unpaged()).getContent();
            log.info("Found {} active products to sync.", activeProducts.size());
            
            activeProducts.forEach(productService::syncToElastic);
            
            log.info("Initial synchronization completed successfully.");
        } catch (Exception e) {
            log.error("Error during initial Elasticsearch synchronization: {}", e.getMessage());
        }
    }
}
