package com.swaply.productservice.controller;

import com.swaply.productservice.dto.ApiResponse;
import com.swaply.productservice.dto.discount.DiscountRuleDto;
import com.swaply.productservice.dto.discount.DiscountValidationResponse;
import com.swaply.productservice.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;


    @PostMapping("/seller")
    public ResponseEntity<ApiResponse<DiscountRuleDto>> createSellerDiscount(@RequestBody @Valid DiscountRuleDto dto) {
        return ResponseEntity.ok(ApiResponse.success(discountService.createDiscountRule(dto)));
    }

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<DiscountRuleDto>> createPlatformDiscount(@RequestBody @Valid DiscountRuleDto dto) {

        DiscountRuleDto platformDto = DiscountRuleDto.builder()
                .code(dto.getCode())
                .type(dto.getType())
                .value(dto.getValue())
                .minOrderAmount(dto.getMinOrderAmount())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .productId(dto.getProductId())
                .minQuantity(dto.getMinQuantity())
                .sellerId(null)
                .build();
        return ResponseEntity.ok(ApiResponse.success(discountService.createDiscountRule(platformDto)));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<DiscountRuleDto>>> getSellerDiscounts(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.success(discountService.getSellerDiscounts(sellerId)));
    }

    @GetMapping("/platform")
    public ResponseEntity<ApiResponse<List<DiscountRuleDto>>> getPlatformDiscounts() {
        return ResponseEntity.ok(ApiResponse.success(discountService.getPlatformDiscounts()));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<DiscountValidationResponse>> validate(
            @RequestParam String code,
            @RequestParam UUID productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(ApiResponse.success(discountService.validateAndCalculate(code, productId, quantity)));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<DiscountValidationResponse>> validateByCode(
            @PathVariable String code,
            @RequestParam BigDecimal totalAmount) {
        return ResponseEntity.ok(ApiResponse.success(discountService.validateByTotalAmount(code, totalAmount)));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<DiscountRuleDto>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(discountService.deactivateDiscount(id)));
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<DiscountRuleDto>>> getReport(
            @RequestParam(required = false) UUID sellerId) {
        return ResponseEntity.ok(ApiResponse.success(discountService.getDiscountReport(sellerId)));
    }
}
