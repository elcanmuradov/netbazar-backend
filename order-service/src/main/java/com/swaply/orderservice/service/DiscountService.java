package com.swaply.orderservice.service;

import com.swaply.orderservice.client.ProductClient;
import com.swaply.orderservice.dto.discount.DiscountRuleDto;
import com.swaply.orderservice.dto.discount.DiscountValidationResponse;
import com.swaply.orderservice.entity.DiscountRule;
import com.swaply.orderservice.exception.NotFoundException;
import com.swaply.orderservice.repository.jpa.DiscountRuleRepository;
import com.swaply.orderservice.utils.enums.DiscountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRuleRepository discountRuleRepository;
    private final ProductClient productClient;

    @Transactional
    public DiscountRuleDto createDiscountRule(DiscountRuleDto dto) {
        DiscountRule rule = DiscountRule.builder()
                .code(dto.getCode())
                .type(dto.getType())
                .value(dto.getValue())
                .minOrderAmount(dto.getMinOrderAmount())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .sellerId(dto.getSellerId())
                .productId(dto.getProductId())
                .minQuantity(dto.getMinQuantity())
                .isActive(true)
                .build();

        DiscountRule saved = discountRuleRepository.save(rule);
        return mapToDto(saved);
    }

    public List<DiscountRuleDto> getSellerDiscounts(UUID sellerId) {
        return discountRuleRepository.findAllBySellerId(sellerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<DiscountRuleDto> getPlatformDiscounts() {
        return discountRuleRepository.findAllBySellerIdIsNull().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DiscountValidationResponse validateAndCalculate(String code, UUID productId, Integer quantity) {
        return validateAndCalculate(code, productId, quantity, null);
    }

    public DiscountValidationResponse validateAndCalculate(String code, UUID productId, Integer quantity, BigDecimal baseAmount) {
        ProductClient.ProductSummary product = getProductOrThrow(productId);
        log.info(product.toString());

        BigDecimal orderAmount = baseAmount != null
                ? baseAmount
                : product.priceAsBigDecimal().multiply(BigDecimal.valueOf(quantity));

        if (code == null || code.isEmpty()) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("No code provided")
                    .discountAmount(BigDecimal.ZERO)
                    .finalPrice(orderAmount)
                    .build();
        }

        DiscountRule rule = discountRuleRepository.findByCodeAndIsActiveTrue(code)
                .orElse(null);

        if (rule == null) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Invalid or inactive promo code")
                    .discountAmount(BigDecimal.ZERO)
                    .finalPrice(orderAmount)
                    .build();
        }
        log.info(rule.toString());
        LocalDateTime now = LocalDateTime.now();
        if (rule.getStartDate() != null && now.isBefore(rule.getStartDate())) {
            return DiscountValidationResponse.builder().valid(false).message("Discount not started yet").build();
        }
        if (rule.getEndDate() != null && now.isAfter(rule.getEndDate())) {
            return DiscountValidationResponse.builder().valid(false).message("Discount expired").build();
        }

        if (rule.getUsageLimit() != null && rule.getUsageCount() >= rule.getUsageLimit()) {
            return DiscountValidationResponse.builder().valid(false).message("Usage limit reached").build();
        }

        if (rule.getMinOrderAmount() != null && orderAmount.compareTo(rule.getMinOrderAmount()) < 0) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Minimum order amount not met: " + rule.getMinOrderAmount())
                    .build();
        }

        if (rule.getSellerId() != null && !rule.getSellerId().equals(product.userId())) {
            return DiscountValidationResponse.builder().valid(false).message("Code not valid for this seller").build();
        }

        if (rule.getMinQuantity() != null && quantity < rule.getMinQuantity()) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Minimum quantity not met: " + rule.getMinQuantity())
                    .build();
        }

        BigDecimal discountAmount;
        if (rule.getType() == DiscountType.PERCENTAGE) {
            discountAmount = orderAmount.multiply(rule.getValue()).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        } else {
            discountAmount = rule.getValue();
        }

        if (rule.getMaxDiscountAmount() != null && discountAmount.compareTo(rule.getMaxDiscountAmount()) > 0) {
            discountAmount = rule.getMaxDiscountAmount();
        }

        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }

        return DiscountValidationResponse.builder()
                .valid(true)
                .message("Discount applied successfully")
                .discountAmount(discountAmount)
                .finalPrice(orderAmount.subtract(discountAmount))
                .type(rule.getType())
                .value(rule.getValue())
                .code(code)
                .build();
    }

    public DiscountValidationResponse validateByTotalAmount(String code, BigDecimal totalAmount, UUID sellerId) {
        if (code == null || code.isEmpty()) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("No code provided")
                    .discountAmount(BigDecimal.ZERO)
                    .finalPrice(totalAmount)
                    .build();
        }

        DiscountRule rule = discountRuleRepository.findByCodeAndIsActiveTrue(code)
                .orElse(null);

        if (rule == null) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Promo kod etibarsızdır")
                    .discountAmount(BigDecimal.ZERO)
                    .finalPrice(totalAmount)
                    .build();
        }

        // Validate dates
        LocalDateTime now = LocalDateTime.now();
        if (rule.getStartDate() != null && now.isBefore(rule.getStartDate())) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Endirim hələ başlamamışdır")
                    .build();
        }
        if (rule.getEndDate() != null && now.isAfter(rule.getEndDate())) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Endirim müddəti sona çatmışdır")
                    .build();
        }

        // Validate usage limit
        if (rule.getUsageLimit() != null && rule.getUsageCount() >= rule.getUsageLimit()) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Endirimin istifadə limiti çatmışdır")
                    .build();
        }

        // Validate min order amount
        if (rule.getMinOrderAmount() != null && totalAmount.compareTo(rule.getMinOrderAmount()) < 0) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Minimum sifariş məbləği: " + rule.getMinOrderAmount() + " ₼")
                    .build();
        }

        // Seller-specific discounts must be matched against the seller that owns the cart items.
        if (rule.getSellerId() != null) {
            if (sellerId == null || !rule.getSellerId().equals(sellerId)) {
                return DiscountValidationResponse.builder()
                        .valid(false)
                        .message("Bu promo kod bu satıcı üçün keçərli deyil")
                        .build();
            }
        }

        // Reject product-specific discounts for bulk orders.
        if (rule.getProductId() != null) {
            return DiscountValidationResponse.builder()
                    .valid(false)
                    .message("Bu promo kod toplu sifarişlər üçün keçərli deyil")
                    .build();
        }

        // Calculate discount
        BigDecimal discountAmount;
        if (rule.getType() == DiscountType.PERCENTAGE) {
            discountAmount = totalAmount.multiply(rule.getValue()).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        } else {
            discountAmount = rule.getValue();
        }

        // Cap discount
        if (rule.getMaxDiscountAmount() != null && discountAmount.compareTo(rule.getMaxDiscountAmount()) > 0) {
            discountAmount = rule.getMaxDiscountAmount();
        }

        // Ensure discount doesn't exceed order amount
        if (discountAmount.compareTo(totalAmount) > 0) {
            discountAmount = totalAmount;
        }

        return DiscountValidationResponse.builder()
                .valid(true)
                .message("Promo kod uğurla tətbiq edildi")
                .discountAmount(discountAmount)
                .finalPrice(totalAmount.subtract(discountAmount))
                .type(rule.getType())
                .value(rule.getValue())
                .code(code)
                .build();
    }

    @Transactional
    public void incrementUsage(String code) {
        discountRuleRepository.findByCodeAndIsActiveTrue(code).ifPresent(rule -> {
            rule.setUsageCount(rule.getUsageCount() + 1);
            discountRuleRepository.save(rule);
        });
    }

    public List<DiscountRuleDto> getDiscountReport(UUID sellerId) {
        List<DiscountRule> rules = sellerId != null
                ? discountRuleRepository.findAllBySellerId(sellerId)
                : discountRuleRepository.findAllBySellerIdIsNull();
        return rules.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<DiscountRuleDto> searchDiscountsBySellerUsername(String username) {
        // This method will search for discounts by seller username
        // Note: In a real implementation, you would call UserClient to get seller ID by username
        // For now, this is a placeholder that can be extended
        return List.of();
    }

    public DiscountRuleDto getDiscountInfo(String code) {
        DiscountRule rule = discountRuleRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new NotFoundException("Discount rule not found: " + code));
        return mapToDto(rule);
    }

    @Transactional
    public DiscountRuleDto deactivateDiscount(UUID id) {
        DiscountRule rule = discountRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Discount rule not found: " + id));
        rule.setIsActive(false);
        return mapToDto(discountRuleRepository.save(rule));
    }

    private DiscountRuleDto mapToDto(DiscountRule rule) {
        return DiscountRuleDto.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .type(rule.getType())
                .value(rule.getValue())
                .minOrderAmount(rule.getMinOrderAmount())
                .maxDiscountAmount(rule.getMaxDiscountAmount())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .usageLimit(rule.getUsageLimit())
                .usageCount(rule.getUsageCount())
                .sellerId(rule.getSellerId())
                .productId(rule.getProductId())
                .minQuantity(rule.getMinQuantity())
                .isActive(rule.getIsActive())
                .build();
    }

    private ProductClient.ProductSummary getProductOrThrow(UUID productId) {
        try {
            var response = productClient.getProductById(productId);
            if (response == null || response.getData() == null) {
                throw new NotFoundException("Product not found: " + productId);
            }
            return response.getData();
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new NotFoundException("Product not found: " + productId);
        }
    }
}

