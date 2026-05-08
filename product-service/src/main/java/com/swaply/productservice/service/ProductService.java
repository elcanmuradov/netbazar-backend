package com.swaply.productservice.service;

import com.swaply.productservice.client.ChatClient;
import com.swaply.productservice.client.MediaClient;
import com.swaply.productservice.client.NotificationClient;
import com.swaply.productservice.client.UserClient;
import com.swaply.productservice.document.ProductDocument;
import com.swaply.productservice.dto.PagedResponse;
import com.swaply.productservice.dto.ProductDto;
import com.swaply.productservice.dto.UpdateProductRequest;
import com.swaply.productservice.dto.create.CreateProductRequest;
import com.swaply.productservice.dto.create.CreateProductResponse;
import com.swaply.productservice.entity.Product;
import com.swaply.productservice.entity.ProductImage;
import com.swaply.productservice.exception.NotFoundException;
import com.swaply.productservice.mapper.ProductMapper;
import com.swaply.productservice.entity.Campaign;
import com.swaply.productservice.repository.elastic.ElasticProductRepository;
import com.swaply.productservice.repository.jpa.CampaignRepository;
import com.swaply.productservice.repository.jpa.ProductRepository;
import com.swaply.productservice.utils.enums.City;
import com.swaply.productservice.utils.enums.ProductCategory;
import com.swaply.productservice.utils.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.swaply.productservice.utils.messages.Constants.PRODUCT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserClient userClient;
    private final ElasticProductRepository elasticProductRepository;
    private final ProductImageAsyncService productImageAsyncService;
    private final MediaClient mediaClient;
    private final ChatClient chatClient;
    private final NotificationAsyncService notificationAsyncService;
    private final CampaignRepository campaignRepository;

    @CacheEvict(value = "products", allEntries = true)
    public CreateProductResponse createProduct(CreateProductRequest createProductRequest, List<MultipartFile> files) {
        UserClient.UserSummary owner = Optional.ofNullable(userClient.getSellerById(createProductRequest.getUserId()).getData())
                .orElseThrow(() -> new NotFoundException("Seller not found: " + createProductRequest.getUserId()));

        String roleText = Optional.ofNullable(owner.userRole()).orElse("").toUpperCase();
        if (!roleText.contains("MARKET") && !roleText.contains("ADMIN") && !roleText.contains("SELLER")) {
            throw new IllegalStateException("Yalnız market hesabı məhsul əlavə edə bilər");
        }

        Product product = new Product();
        product.setTitle(createProductRequest.getTitle());
        product.setPrice(createProductRequest.getPrice());
        product.setDescription(createProductRequest.getDescription());
        product.setCategory(getProductCategoryById(createProductRequest.getCategoryId()));
        product.setStatus(ProductStatus.ACTIVE);
        product.setUserId(createProductRequest.getUserId());
        product.setCity(getCityById(createProductRequest.getCityId()));
        product.setIsNew(createProductRequest.getIsNew());
        product.setIsDelivery(createProductRequest.getIsDelivery());

        productRepository.save(product);

        if (files != null && !files.isEmpty()) {
            List<MultipartFile> asyncSafeFiles = files.stream()
                    .filter(Objects::nonNull)
                    .filter(file -> !file.isEmpty())
                    .map(this::toInMemoryMultipartFile)
                    .toList();
            if (!asyncSafeFiles.isEmpty()) {
                productImageAsyncService.uploadProductImagesAsync(asyncSafeFiles, product.getId());
            }
        }
        productImageAsyncService.syncProductToElasticAsync(product.getId());

        return CreateProductResponse.builder()
                .id(product.getId())
                .userId(product.getUserId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .isNew(product.getIsNew())
                .category(product.getCategory())
                .isDelivery(product.getIsDelivery())
                .createdAt(product.getCreatedAt())
                .viewers(product.getViewers())
                .city(product.getCity())
                .status(product.getStatus())
                .images(new ArrayList<>())
                .build();
    }

    private MultipartFile toInMemoryMultipartFile(MultipartFile file) {
        try {
            return new InMemoryMultipartFile(
                    file.getName(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to buffer uploaded file in memory", e);
        }
    }


    public List<ProductDto> getFavoriteProducts() {
        List<UUID> productsId = userClient.findAllFavorites();
        return productsId.stream()
                .map(this::getProductById)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(UUID productId) {
        log.info("Get product by id {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));
        product.setViewers(product.getViewers() + 1);
        product = productRepository.save(product);
        log.info("Product by id {}", product.getId());
        ProductDto dto = productMapper.toDto(product);
        applyCampaignDiscounts(dto);
        return dto;
    }


    public PagedResponse<ProductDto> getProductsByUserId(UUID userId, Pageable pageable) {
        log.info("get user products, page: {}", pageable.getPageNumber());
        Page<Product> page = productRepository.getProductByUserId(userId, pageable);
        return mapToPagedResponse(page);
    }

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResponse<ProductDto> getActiveProducts(Pageable pageable) {
        log.info("getting active products, page: {}", pageable.getPageNumber());
        Page<Product> page = productRepository.getProductsByStatus(ProductStatus.ACTIVE, pageable);
        return mapToPagedResponse(page);
    }

    public PagedResponse<ProductDto> getAllPaged(Pageable pageable) {
        log.info("getting all products paged");
        Page<Product> page = productRepository.findAll(pageable);
        return mapToPagedResponse(page);
    }

    public PagedResponse<ProductDto> getProductsByStatusAndUserId(ProductStatus status, UUID userId, Pageable pageable) {
        log.info("getting products by status {} and user id {}, page: {}", status, userId, pageable.getPageNumber());
        Page<Product> productPage = productRepository.getProductsByStatusAndUserId(status, userId, pageable);
        return mapToPagedResponse(productPage);
    }

    public PagedResponse<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        log.info("getting products by category {}, page: {}", category, pageable.getPageNumber());
        Page<Product> page = productRepository.getProductsByCategoryAndStatus(category, ProductStatus.ACTIVE, pageable);
        return mapToPagedResponse(page);
    }

    public PagedResponse<ProductDto> getDeletedProductByUserId(UUID userId, Pageable pageable) {
        return getProductsByStatusAndUserId(ProductStatus.DELETED, userId, pageable);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteByID(UUID uuid) {
        log.info("Deleting product with id: {}", uuid);
        Product product = productRepository.getProductById(uuid)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + uuid));
        product.setStatus(ProductStatus.DELETED);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        syncToElastic(product);
    }

    private ProductCategory getProductCategoryById(Integer categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Category ID cannot be null");
        ProductCategory[] categories = ProductCategory.values();
        if (categoryId < 0 || categoryId >= categories.length)
            throw new IllegalArgumentException("Invalid category ID: " + categoryId);
        return categories[categoryId];
    }

    private City getCityById(Integer cityId) {
        if (cityId == null) throw new IllegalArgumentException("City ID cannot be null");
        City[] cities = City.values();
        if (cityId < 0 || cityId >= cities.length) throw new IllegalArgumentException("Invalid city ID: " + cityId);
        return cities[cityId];
    }

    public Long getProductCount() {
        return productRepository.count();
    }

    public void deleteProduct(UUID productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));
        UUID ownerId = product.getUserId();
        String productTitle = product.getTitle();

        List<String> publicIds = product.getImages().stream()
                .map(ProductImage::getPublicId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toList());

        if (!publicIds.isEmpty()) mediaClient.deleteImages(publicIds);
        productRepository.deleteById(productId);
        elasticProductRepository.deleteById(String.valueOf(productId));

        String effectiveReason = (reason == null || reason.isBlank()) ? "Qayda pozuntusu" : reason.trim();
        if (ownerId != null) {
            UserClient.UserSummary owner = Optional.ofNullable(userClient.getSellerById(ownerId).getData()).orElse(null);
            if (owner != null && owner.email() != null && !owner.email().isBlank()) {
                notificationAsyncService.sendProductDeletedEmailAsync(
                        new NotificationClient.ProductDeletedEmailRequest(owner.email(), productTitle, effectiveReason)
                );
            }
        }
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto setActive(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));
        product.setStatus(ProductStatus.ACTIVE);
        product.setDeletedAt(null);
        productRepository.save(product);
        syncToElastic(product);
        return productMapper.toDto(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductDto updateProduct(UpdateProductRequest request, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));

        Optional.ofNullable(request.getIsDelivery()).ifPresent(product::setIsDelivery);
        Optional.ofNullable(getProductCategoryById(request.getCategoryId())).ifPresent(product::setCategory);
        Optional.ofNullable(getCityById(request.getCityId())).ifPresent(product::setCity);
        Optional.ofNullable(request.getDescription()).ifPresent(product::setDescription);
        Optional.ofNullable(request.getPrice()).ifPresent(product::setPrice);
        Optional.ofNullable(request.getTitle()).ifPresent(product::setTitle);
        Optional.ofNullable(request.getIsNew()).ifPresent(product::setIsNew);

        if (request.getImages() != null) {
            List<ProductImage> currentImages = product.getImages() != null ? product.getImages() : new ArrayList<>();

            Map<String, ProductImage> byImageUrl = currentImages.stream()
                    .filter(img -> img.getImageUrl() != null)
                    .collect(Collectors.toMap(ProductImage::getImageUrl, img -> img, (a, b) -> a));

            Set<String> requestedUrls = request.getImages().stream()
                    .map(UpdateProductRequest.ImageRequest::getImageUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<String> removedPublicIds = currentImages.stream()
                    .filter(img -> img.getImageUrl() != null && !requestedUrls.contains(img.getImageUrl()))
                    .map(ProductImage::getPublicId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toList());

            if (!removedPublicIds.isEmpty()) {
                mediaClient.deleteImages(removedPublicIds);
            }

            List<ProductImage> reorderedImages = new ArrayList<>();
            for (int i = 0; i < request.getImages().size(); i++) {
                UpdateProductRequest.ImageRequest imageRequest = request.getImages().get(i);
                if (imageRequest.getImageUrl() == null || imageRequest.getImageUrl().isBlank()) {
                    continue;
                }

                ProductImage existing = byImageUrl.get(imageRequest.getImageUrl());
                ProductImage image = existing != null ? existing : new ProductImage();

                image.setImageUrl(imageRequest.getImageUrl());
                image.setPublicId(imageRequest.getPublicId() != null ? imageRequest.getPublicId() : image.getPublicId());
                image.setDisplayOrder(i);
                image.setIsMain(i == 0);
                image.setProduct(product);
                reorderedImages.add(image);
            }

            List<ProductImage> managedImages = product.getImages();
            managedImages.clear();
            managedImages.addAll(reorderedImages);
        }

        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        syncToElastic(product);
        return productMapper.toDto(product);
    }

    public List<ProductDto> searchProducts(String query) {
        log.info("Searching products with ElasticSearch, query: {}", query);
        List<ProductDocument> results = elasticProductRepository.searchActiveProducts(query);
        List<ProductDto> products = new ArrayList<>();
        results.forEach(product -> products.add(documentToDto(product)));
        return products;
    }

    public ProductDto documentToDto(ProductDocument doc) {
        ProductDto dto = ProductDto.builder()
                .id(doc.getId() != null ? UUID.fromString(doc.getId()) : null)
                .userId(doc.getUserId() != null ? UUID.fromString(doc.getUserId()) : null)
                .title(doc.getTitle())
                .description(doc.getDescription())
                .price(doc.getPrice())
                .isNew(doc.getIsNew() != null ? doc.getIsNew() : false)
                .isDelivery(doc.getIsDelivery() != null ? doc.getIsDelivery() : false)
                .category(doc.getCategory())
                .createdAt(doc.getCreatedAt() != null ? new Date(doc.getCreatedAt()) : null)
                .city(doc.getCity())
                .status(doc.getStatus())
                .imageUrls(doc.getImageUrls())
                .build();
        applyCampaignDiscounts(dto);
        return dto;
    }

    private void applyCampaignDiscounts(ProductDto dto) {
        if (dto == null || dto.getPrice() == null) return;

        List<Campaign> activeCampaigns = campaignRepository.findAllByIsActiveTrueOrderByStartDateDesc();
        LocalDateTime now = LocalDateTime.now();

        // Find the best applicable campaign
        Campaign bestCampaign = activeCampaigns.stream()
                .filter(c -> (c.getStartDate() == null || now.isAfter(c.getStartDate())) &&
                             (c.getEndDate() == null || now.isBefore(c.getEndDate())))
                .findFirst() // Just take the latest active campaign for now
                .orElse(null);

        if (bestCampaign != null) {
            double originalPrice = dto.getPrice();
            double discountAmount = 0;

            if ("PERCENTAGE".equalsIgnoreCase(bestCampaign.getDiscountType())) {
                discountAmount = (originalPrice * bestCampaign.getDiscountValue().doubleValue()) / 100.0;
                dto.setDiscountPercentage(bestCampaign.getDiscountValue().intValue());
            } else {
                discountAmount = bestCampaign.getDiscountValue().doubleValue();
                dto.setDiscountPercentage((int) ((discountAmount / originalPrice) * 100));
            }

            dto.setDiscountedPrice(Math.max(0, originalPrice - discountAmount));
        } else {
            dto.setDiscountedPrice(dto.getPrice());
            dto.setDiscountPercentage(0);
        }
    }

    public void syncToElastic(Product product) {
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
                    .createdAt(product.getCreatedAt() != null ?
                            product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                    .imageUrls(orderedImageUrls)
                    .build();
            elasticProductRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to sync product to ElasticSearch: {}", e.getMessage());
        }
    }

    private PagedResponse<ProductDto> mapToPagedResponse(Page<Product> page) {
        List<ProductDto> content = page.getContent().stream()
                .map(product -> {
                    ProductDto dto = productMapper.toDto(product);
                    applyCampaignDiscounts(dto);
                    return dto;
                }).collect(Collectors.toList());

        PagedResponse<ProductDto> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        return response;
    }

    public Boolean isActiveProduct(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));
        return product.getStatus().equals(ProductStatus.ACTIVE);
    }

    public Void reportProduct(UUID productId) {
        Product product = productRepository.getProductById(productId).orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));
        product.setReportCount(product.getReportCount() + 1);
        product.setIsReported(true);
        productRepository.save(product);
        return null;
    }


    public Void markResolved(UUID productId) {
        Product product = productRepository.getProductById(productId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND + productId));

        product.setIsReported(false);
        product.setReportCount(0);
        productRepository.save(product);
        return null;
    }

    public List<ProductDto> findAllReportedProducts() {
        return productRepository.getProductsByIsReported(true)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsReported()) && p.getReportCount() != null && p.getReportCount() > 0)
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getProductsByStatus(ProductStatus status) {
        return productRepository.findAll().stream()
                .filter(p -> p.getStatus() == status)
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * CSV columns: title, description, price, categoryId, cityId [, isNew, isDelivery]
     */
    public void createProductFromCsv(UUID sellerId, String[] cols) {
        Product product = new Product();
        product.setUserId(sellerId);
        product.setTitle(cols[0].trim());
        product.setDescription(cols.length > 1 ? cols[1].trim() : "");
        product.setPrice(new java.math.BigDecimal(cols[2].trim()));
        product.setCategory(getProductCategoryById(Integer.parseInt(cols[3].trim())));
        product.setCity(getCityById(Integer.parseInt(cols[4].trim())));
        product.setIsNew(cols.length > 5 && "true".equalsIgnoreCase(cols[5].trim()));
        product.setIsDelivery(cols.length > 6 && "true".equalsIgnoreCase(cols[6].trim()));
        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }
}
