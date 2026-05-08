package com.swaply.productservice.controller;

import com.swaply.productservice.dto.ApiResponse;
import com.swaply.productservice.dto.PagedResponse;
import com.swaply.productservice.dto.ProductDto;
import com.swaply.productservice.dto.UpdateProductRequest;
import com.swaply.productservice.dto.create.CreateProductRequest;
import com.swaply.productservice.dto.create.CreateProductResponse;
import com.swaply.productservice.service.ProductService;
import com.swaply.productservice.utils.enums.ProductCategory;
import com.swaply.productservice.utils.enums.ProductStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> findAllActiveProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getActiveProducts(pageable)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getAllPaged(pageable)));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> findByCategory(
            @PathVariable ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByCategory(category, pageable)));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<ProductDto>>> findAllFavorites() {
        return ResponseEntity.ok(ApiResponse.success(productService.getFavoriteProducts()));
    }
    @PostMapping(path = "user/{userId}/product/create", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CreateProductResponse>> create(
            @RequestPart("request") @Valid CreateProductRequest request,
            @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(request, files)));
    }

    @GetMapping("user/{userId}/product")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> findByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByUserId(userId, pageable)));
    }

    @GetMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> findById(@PathVariable UUID userId, @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(productId)));
    }

    @GetMapping("user/{userId}/product/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> findByStatusAndUserId(
            @PathVariable UUID userId, 
            @PathVariable ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByStatusAndUserId(status, userId, pageable)));
    }

    @GetMapping("user/{userId}/product/delete")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> findDeletedProductByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getDeletedProductByUserId(userId, pageable)));
    }

    @PutMapping("user/{userId}/product/{productId}/set-active")
    public ResponseEntity<ApiResponse<ProductDto>> setActiveProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.setActive(productId)));
    }


    @DeleteMapping("/user/{userId}/product/{productId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable UUID userId, @PathVariable UUID productId) {
        productService.deleteByID(productId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/user/delete-from-db/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID productId, @RequestParam(value = "reason", required = false) String reason) {
        productService.deleteProduct(productId, reason);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


    @GetMapping("/product-count")
    public ResponseEntity<ApiResponse<Long>> getProductCount(){
        return ResponseEntity.ok(ApiResponse.success(productService.getProductCount()));
    }



    @PutMapping("/user/{userId}/product/{productId}/update")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@RequestBody UpdateProductRequest updateProductRequest, @PathVariable UUID userId, @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(updateProductRequest,productId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(q)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductPublic(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(productId)));
    }

    @GetMapping("/product/isActive")
    public ResponseEntity<ApiResponse<Boolean>> isActiveProduct(@RequestParam("uuid") UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.isActiveProduct(productId)));
    }

    @PostMapping("user/{userId}/product/{productId}/report")
    public ResponseEntity<ApiResponse<Void>> reportProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.reportProduct(productId)));
    }

    @PostMapping("user/{userId}/product/{productId}/mark-resolved")
    public ResponseEntity<ApiResponse<Void>> markResolved(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.markResolved(productId)));
    }

    @GetMapping("/all-reported")
    public ResponseEntity<ApiResponse<List<ProductDto>>> findAllReportedProducts(){
            return ResponseEntity.ok(ApiResponse.success(productService.findAllReportedProducts()));
    }



}
