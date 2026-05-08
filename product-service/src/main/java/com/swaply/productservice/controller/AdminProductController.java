package com.swaply.productservice.controller;

import com.swaply.productservice.dto.ApiResponse;
import com.swaply.productservice.dto.ProductDto;
import com.swaply.productservice.dto.campaign.CampaignDto;
import com.swaply.productservice.entity.*;
import com.swaply.productservice.exception.NotFoundException;
import com.swaply.productservice.repository.jpa.*;
import com.swaply.productservice.service.ProductService;
import com.swaply.productservice.service.CommerceService;
import com.swaply.productservice.service.CampaignService;
import com.swaply.productservice.utils.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final BannerRepository bannerRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignService campaignService;
    private final CommerceService commerceService;
    private final BlogPostRepository blogPostRepository;
    private final StaticPageRepository staticPageRepository;
    private final CategoryRepository categoryRepository;
    private final CustomerOrderRepository customerOrderRepository;

    @GetMapping("/kpi")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getKpi() {
        long totalProducts = productService.getProductCount();
        long totalOrders = customerOrderRepository.count();
        BigDecimal totalRevenue = customerOrderRepository.findAll().stream()
                .map(CustomerOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal todayRevenue = customerOrderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null &&
                        o.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .map(CustomerOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long todayOrders = customerOrderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null &&
                        o.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalProducts", totalProducts);
        kpi.put("totalOrders", totalOrders);
        kpi.put("totalRevenue", totalRevenue);
        kpi.put("todayRevenue", todayRevenue);
        kpi.put("todayOrders", todayOrders);
        return ResponseEntity.ok(ApiResponse.success(kpi));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport() {
        return ResponseEntity.ok(ApiResponse.success(commerceService.getMonthlyReport()));
    }

    @GetMapping("/products/pending")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getPendingProducts() {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProductsByStatus(ProductStatus.PENDING)));
    }

    @PutMapping("/products/{productId}/approve")
    public ResponseEntity<ApiResponse<ProductDto>> approveProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.setActive(productId)));
    }

    @DeleteMapping("/products/{productId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectProduct(
            @PathVariable UUID productId,
            @RequestParam(required = false) String reason) {
        productService.deleteProduct(productId, reason);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/products/reported")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getReportedProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.findAllReportedProducts()));
    }

    @PostMapping("/products/{productId}/mark-resolved")
    public ResponseEntity<ApiResponse<Void>> markResolved(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.markResolved(productId)));
    }


    @PostMapping("/products/bulk-import")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> bulkImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam UUID sellerId) {
        int imported = 0;
        int failed = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                try {
                    String[] cols = line.split(",", -1);
                    if (cols.length < 5) { failed++; continue; }
                    productService.createProductFromCsv(sellerId, cols);
                    imported++;
                } catch (Exception e) {
                    failed++;
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("File read error: " + e.getMessage()));
        }
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("imported", imported);
        result.put("failed", failed);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/products/export")
    public ResponseEntity<byte[]> exportProducts() {
        StringBuilder csv = new StringBuilder("id,title,price,category,status,sellerId,createdAt\n");
        productService.getAllProducts().forEach(p ->
                csv.append(p.getId()).append(",")
                        .append(p.getTitle()).append(",")
                        .append(p.getPrice()).append(",")
                        .append(p.getCategory()).append(",")
                        .append(p.getStatus()).append(",")
                        .append(p.getUserId()).append(",")
                        .append(p.getCreatedAt()).append("\n"));
        byte[] bytes = csv.toString().getBytes();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=products.csv")
                .header("Content-Type", "text/csv")
                .body(bytes);
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<CustomerOrder>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<CustomerOrder> orders;
        if (status != null && !status.isBlank()) {
            orders = customerOrderRepository.findAll(PageRequest.of(page, size)).stream()
                    .filter(o -> o.getStatus().equalsIgnoreCase(status))
                    .toList();
        } else {
            orders = customerOrderRepository.findAll(PageRequest.of(page, size)).getContent();
        }
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<CustomerOrder>> getOrderDetail(@PathVariable UUID orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable UUID orderId, @RequestParam String status) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        customerOrderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<Banner>>> getBanners() {
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.findAllByOrderBySortOrderAsc()));
    }

    @GetMapping("/banners/placement/{placement}")
    public ResponseEntity<ApiResponse<List<Banner>>> getBannersByPlacement(@PathVariable String placement) {
        return ResponseEntity.ok(ApiResponse.success(
                bannerRepository.findAllByPlacementAndIsActiveTrueOrderBySortOrderAsc(placement)));
    }

    @PostMapping("/banners")
    public ResponseEntity<ApiResponse<Banner>> createBanner(@RequestBody Banner banner) {
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.save(banner)));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<Banner>> updateBanner(@PathVariable UUID id, @RequestBody Banner banner) {
        banner.setId(id);
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.save(banner)));
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable UUID id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<CampaignDto>>> getCampaigns() {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getAllCampaigns()));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<CampaignDto>> createCampaign(@RequestBody CampaignDto dto) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.createCampaign(dto)));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<CampaignDto>> updateCampaign(@PathVariable UUID id, @RequestBody CampaignDto dto) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.updateCampaign(id, dto)));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable UUID id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/campaigns/{id}/activate")
    public ResponseEntity<ApiResponse<CampaignDto>> activateCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.activateCampaign(id)));
    }

    @PutMapping("/campaigns/{id}/deactivate")
    public ResponseEntity<ApiResponse<CampaignDto>> deactivateCampaign(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.deactivateCampaign(id)));
    }

    @GetMapping("/blog")
    public ResponseEntity<ApiResponse<List<BlogPost>>> getBlogPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                blogPostRepository.findAll(PageRequest.of(page, size)).getContent()));
    }

    @GetMapping("/blog/{id}")
    public ResponseEntity<ApiResponse<BlogPost>> getBlogPost(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                blogPostRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Blog post not found: " + id))));
    }

    @PostMapping("/blog")
    public ResponseEntity<ApiResponse<BlogPost>> createBlogPost(@RequestBody BlogPost post) {
        return ResponseEntity.ok(ApiResponse.success(blogPostRepository.save(post)));
    }

    @PutMapping("/blog/{id}")
    public ResponseEntity<ApiResponse<BlogPost>> updateBlogPost(@PathVariable UUID id, @RequestBody BlogPost post) {
        post.setId(id);
        return ResponseEntity.ok(ApiResponse.success(blogPostRepository.save(post)));
    }

    @DeleteMapping("/blog/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlogPost(@PathVariable UUID id) {
        blogPostRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/pages")
    public ResponseEntity<ApiResponse<List<StaticPage>>> getPages() {
        return ResponseEntity.ok(ApiResponse.success(staticPageRepository.findAll()));
    }

    @GetMapping("/pages/{slug}")
    public ResponseEntity<ApiResponse<StaticPage>> getPage(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(
                staticPageRepository.findBySlug(slug)
                        .orElseThrow(() -> new NotFoundException("Page not found: " + slug))));
    }

    @PostMapping("/pages")
    public ResponseEntity<ApiResponse<StaticPage>> createPage(@RequestBody StaticPage page) {
        return ResponseEntity.ok(ApiResponse.success(staticPageRepository.save(page)));
    }

    @PutMapping("/pages/{slug}")
    public ResponseEntity<ApiResponse<StaticPage>> updatePage(@PathVariable String slug, @RequestBody StaticPage page) {
        StaticPage existing = staticPageRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Page not found: " + slug));
        page.setId(existing.getId());
        return ResponseEntity.ok(ApiResponse.success(staticPageRepository.save(page)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll()));
    }

    @GetMapping("/categories/top-level")
    public ResponseEntity<ApiResponse<List<Category>>> getTopLevelCategories() {
        return ResponseEntity.ok(ApiResponse.success(
                categoryRepository.findAllByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc()));
    }

    @GetMapping("/categories/{parentId}/children")
    public ResponseEntity<ApiResponse<List<Category>>> getSubCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryRepository.findAllByParentIdAndIsActiveTrueOrderBySortOrderAsc(parentId)));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.save(category)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable UUID id, @RequestBody Category category) {
        category.setId(id);
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.save(category)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
