package com.swaply.orderservice.service;

import com.swaply.orderservice.dto.commerce.CreateOrderRequest;
import com.swaply.orderservice.dto.commerce.CreateBulkOrderRequest;
import com.swaply.orderservice.dto.commerce.BulkOrderItemRequest;
import com.swaply.orderservice.dto.commerce.CreateReviewRequest;
import com.swaply.orderservice.dto.commerce.OrderDto;
import com.swaply.orderservice.dto.commerce.ReviewDto;
import com.swaply.orderservice.dto.discount.DiscountValidationResponse;
import com.swaply.orderservice.client.ProductClient;
import com.swaply.orderservice.entity.Campaign;
import com.swaply.orderservice.entity.CustomerOrder;
import com.swaply.orderservice.entity.ProductReview;
import com.swaply.orderservice.exception.NotFoundException;
import com.swaply.orderservice.client.UserClient;
import com.swaply.orderservice.repository.jpa.CampaignRepository;
import com.swaply.orderservice.repository.jpa.CustomerOrderRepository;
import com.swaply.orderservice.repository.jpa.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommerceService {

    private final ProductClient productClient;
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final com.swaply.orderservice.client.ChatClient chatClient;
    private final com.swaply.orderservice.client.UserClient userClient;
    private final DiscountService discountService;
    private final CampaignRepository campaignRepository;


    public List<OrderDto> getOrders(UUID buyerId) {
        return customerOrderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(this::toOrderDto)
                .toList();
    }

    public OrderDto createOrder(UUID buyerId, CreateOrderRequest request) {
        ProductClient.ProductSummary product = getProductOrThrow(request.getProductId());

        int quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;

        BigDecimal originalPrice = product.priceAsBigDecimal().multiply(BigDecimal.valueOf(quantity));
        BigDecimal campaignDiscountAmount = calculateCampaignDiscount(originalPrice);
        BigDecimal priceAfterCampaign = originalPrice.subtract(campaignDiscountAmount).max(BigDecimal.ZERO);

        BigDecimal discountAmount = campaignDiscountAmount;
        BigDecimal finalPrice = priceAfterCampaign;
        String appliedCode = null;

        // Apply discount if promo code provided
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            DiscountValidationResponse validation = discountService.validateAndCalculate(
                request.getDiscountCode(), request.getProductId(), quantity, priceAfterCampaign);
            if (validation.isValid()) {
            BigDecimal promoDiscount = validation.getDiscountAmount() == null ? BigDecimal.ZERO : validation.getDiscountAmount();
            discountAmount = campaignDiscountAmount.add(promoDiscount);
                finalPrice = validation.getFinalPrice();
                appliedCode = request.getDiscountCode();
                discountService.incrementUsage(appliedCode);
            } else {
                throw new IllegalArgumentException(validation.getMessage());
            }
        }

        CustomerOrder order = new CustomerOrder();
        order.setBuyerId(buyerId);
        order.setSellerId(product.userId());
        order.setProductId(product.id());
        order.setProductTitle(product.title());
        order.setQuantity(quantity);
        order.setOriginalPrice(originalPrice);
        order.setDiscountAmount(discountAmount);
        order.setTotalPrice(finalPrice);
        order.setDiscountCode(appliedCode);
        order.setPaymentMethod(request.getPaymentMethod() == null || request.getPaymentMethod().isBlank() ? "Kartla" : request.getPaymentMethod());
        order.setDeliveryCity(request.getDeliveryCity());
        order.setStatus("RECEIVED");

        order = customerOrderRepository.save(order);

        try {
            String buyerName = "Müştəri";
            chatClient.sendOrderPlacedNotice(new com.swaply.orderservice.client.ChatClient.OrderNoticeRequest(
                    product.userId(),
                    buyerName,
                    product.title()
            ));
        } catch (Exception e) {
            System.err.println("Failed to send order notification: " + e.getMessage());
        }

        return toOrderDto(order);
    }

    public List<OrderDto> createBulkOrder(UUID buyerId, CreateBulkOrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sifariş səbəti boşdur");
        }

        List<PreparedItem> preparedItems = new ArrayList<>();
        BigDecimal totalAfterCampaign = BigDecimal.ZERO;
        UUID cartSellerId = null;
        boolean hasMultipleSellers = false;

        for (BulkOrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest == null || itemRequest.getProductId() == null) {
                throw new IllegalArgumentException("Məhsul məlumatı natamamdır");
            }

            ProductClient.ProductSummary product = getProductOrThrow(itemRequest.getProductId());

            if (cartSellerId == null) {
                cartSellerId = product.userId();
            } else if (!cartSellerId.equals(product.userId())) {
                hasMultipleSellers = true;
            }

            int quantity = itemRequest.getQuantity() != null && itemRequest.getQuantity() > 0 ? itemRequest.getQuantity() : 1;

            BigDecimal originalPrice = product.priceAsBigDecimal().multiply(BigDecimal.valueOf(quantity));
            BigDecimal campaignDiscountAmount = calculateCampaignDiscount(originalPrice);
            BigDecimal priceAfterCampaign = originalPrice.subtract(campaignDiscountAmount).max(BigDecimal.ZERO);

            PreparedItem preparedItem = new PreparedItem();
            preparedItem.product = product;
            preparedItem.quantity = quantity;
            preparedItem.originalPrice = originalPrice;
            preparedItem.campaignDiscountAmount = campaignDiscountAmount;
            preparedItem.priceAfterCampaign = priceAfterCampaign;

            preparedItems.add(preparedItem);
            totalAfterCampaign = totalAfterCampaign.add(priceAfterCampaign);
        }

        BigDecimal promoDiscountTotal = BigDecimal.ZERO;
        String appliedCode = null;

        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            UUID sellerScopeId = hasMultipleSellers ? null : cartSellerId;
            DiscountValidationResponse validation = discountService.validateByTotalAmount(
                    request.getDiscountCode(),
                    totalAfterCampaign,
                    sellerScopeId
            );
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getMessage());
            }

            promoDiscountTotal = validation.getDiscountAmount() == null ? BigDecimal.ZERO : validation.getDiscountAmount();
            promoDiscountTotal = promoDiscountTotal.min(totalAfterCampaign).max(BigDecimal.ZERO);
            appliedCode = request.getDiscountCode();
        }

        Map<UUID, BigDecimal> promoShareByProductId = distributePromoDiscount(preparedItems, totalAfterCampaign, promoDiscountTotal);

        List<OrderDto> result = new ArrayList<>();
        for (PreparedItem preparedItem : preparedItems) {
            BigDecimal promoShare = promoShareByProductId.getOrDefault(preparedItem.product.id(), BigDecimal.ZERO);
            BigDecimal totalDiscount = preparedItem.campaignDiscountAmount.add(promoShare);
            BigDecimal finalPrice = preparedItem.originalPrice.subtract(totalDiscount).max(BigDecimal.ZERO);

            CustomerOrder order = new CustomerOrder();
            order.setBuyerId(buyerId);
            order.setSellerId(preparedItem.product.userId());
            order.setProductId(preparedItem.product.id());
            order.setProductTitle(preparedItem.product.title());
            order.setQuantity(preparedItem.quantity);
            order.setOriginalPrice(preparedItem.originalPrice);
            order.setDiscountAmount(totalDiscount);
            order.setTotalPrice(finalPrice);
            order.setDiscountCode(appliedCode);
            order.setPaymentMethod(request.getPaymentMethod() == null || request.getPaymentMethod().isBlank() ? "Kartla" : request.getPaymentMethod());
            order.setDeliveryCity(request.getDeliveryCity());
            order.setStatus("RECEIVED");

            order = customerOrderRepository.save(order);
            result.add(toOrderDto(order));

            try {
                String buyerName = "Müştəri";
                chatClient.sendOrderPlacedNotice(new com.swaply.orderservice.client.ChatClient.OrderNoticeRequest(
                    preparedItem.product.userId(),
                        buyerName,
                    preparedItem.product.title()
                ));
            } catch (Exception e) {
                System.err.println("Failed to send order notification: " + e.getMessage());
            }
        }

        if (appliedCode != null) {
            discountService.incrementUsage(appliedCode);
        }

        return result;
    }

    public List<ReviewDto> getReviews(UUID productId) {
        return productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toReviewDto)
                .toList();
    }

    public ReviewDto createReview(UUID productId, UUID userId, String userName, CreateReviewRequest request) {
        ProductClient.ProductSummary product = getProductOrThrow(productId);

        int rating = request.getRating() == null ? 5 : request.getRating();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        ProductReview review = new ProductReview();
        review.setProductId(product.id());
        review.setUserId(userId);
        review.setUserName(userName == null || userName.isBlank() ? "Anonim" : userName);
        review.setRating(rating);
        review.setComment(request.getComment() == null ? "" : request.getComment().trim());

        review = productReviewRepository.save(review);

        return toReviewDto(review);
    }

    private BigDecimal calculateCampaignDiscount(BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        Campaign campaign = getBestActiveCampaign();
        if (campaign == null || campaign.getDiscountValue() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if ("PERCENTAGE".equalsIgnoreCase(campaign.getDiscountType())) {
            BigDecimal percent = campaign.getDiscountValue().max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
            discount = originalPrice.multiply(percent).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        } else {
            discount = campaign.getDiscountValue().max(BigDecimal.ZERO);
        }

        return discount.min(originalPrice);
    }

    private Campaign getBestActiveCampaign() {
        LocalDateTime now = LocalDateTime.now();
        return campaignRepository.findAllByIsActiveTrueOrderByStartDateDesc()
                .stream()
                .filter(c -> (c.getStartDate() == null || !now.isBefore(c.getStartDate())) &&
                        (c.getEndDate() == null || !now.isAfter(c.getEndDate())))
                .findFirst()
                .orElse(null);
    }

    private Map<UUID, BigDecimal> distributePromoDiscount(List<PreparedItem> preparedItems,
                                                           BigDecimal totalAfterCampaign,
                                                           BigDecimal promoDiscountTotal) {
        Map<UUID, BigDecimal> result = new HashMap<>();

        if (promoDiscountTotal == null || promoDiscountTotal.compareTo(BigDecimal.ZERO) <= 0 ||
                totalAfterCampaign == null || totalAfterCampaign.compareTo(BigDecimal.ZERO) <= 0) {
            return result;
        }

        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < preparedItems.size(); i++) {
            PreparedItem preparedItem = preparedItems.get(i);

            BigDecimal share;
            if (i == preparedItems.size() - 1) {
                share = promoDiscountTotal.subtract(allocated).max(BigDecimal.ZERO);
            } else {
                share = promoDiscountTotal
                        .multiply(preparedItem.priceAfterCampaign)
                        .divide(totalAfterCampaign, 2, RoundingMode.HALF_UP)
                        .min(preparedItem.priceAfterCampaign)
                        .max(BigDecimal.ZERO);
                allocated = allocated.add(share);
            }

            result.put(preparedItem.product.id(), share);
        }

        return result;
    }

    private static class PreparedItem {
        private ProductClient.ProductSummary product;
        private int quantity;
        private BigDecimal originalPrice;
        private BigDecimal campaignDiscountAmount;
        private BigDecimal priceAfterCampaign;
    }

    public List<OrderDto> getSellerOrders(UUID sellerId) {
        return customerOrderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(this::toOrderDto)
                .toList();
    }

    public OrderDto updateOrderStatus(UUID orderId, String status) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        order = customerOrderRepository.save(order);
        return toOrderDto(order);
    }

    public Map<String, Object> getSellerStats(UUID sellerId) {
        List<CustomerOrder> orders = customerOrderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        long activeProducts = getActiveProductCount(sellerId);
        long dailyOrders = orders.stream()
                .filter(o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
                .count();
        long ordersInProgress = orders.stream()
                .filter(o -> !o.getStatus().equals("DELIVERED") && !o.getStatus().equals("CANCELLED"))
                .count();
        BigDecimal revenueToday = orders.stream()
                .filter(o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
                .map(CustomerOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<CustomerOrder> currentMonthOrders = orders.stream()
                .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(currentMonth))
                .toList();
        List<CustomerOrder> previousMonthOrders = orders.stream()
                .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(previousMonth))
                .toList();

        BigDecimal commissionRate = getSellerCommissionRate(sellerId);
        BigDecimal monthlyRevenue = sumOrderTotals(currentMonthOrders);
        BigDecimal previousMonthlyRevenue = sumOrderTotals(previousMonthOrders);
        BigDecimal monthlyCommission = calculateCommissionAmount(monthlyRevenue, commissionRate);
        BigDecimal previousMonthlyCommission = calculateCommissionAmount(previousMonthlyRevenue, commissionRate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeProducts", activeProducts);
        stats.put("dailyOrders", dailyOrders);
        stats.put("ordersInProgress", ordersInProgress);
        stats.put("revenueToday", revenueToday);
        stats.put("monthlyRevenue", monthlyRevenue);
        stats.put("previousMonthlyRevenue", previousMonthlyRevenue);
        stats.put("monthlyOrders", (long) currentMonthOrders.size());
        stats.put("previousMonthlyOrders", (long) previousMonthOrders.size());
        stats.put("commissionRate", commissionRate);
        stats.put("monthlyCommissionDue", monthlyCommission);
        stats.put("previousMonthlyCommissionDue", previousMonthlyCommission);
        stats.put("monthlyNetPayout", monthlyRevenue.subtract(monthlyCommission));
        return stats;
    }

    public Map<String, Object> getMonthlyReport() {
        List<CustomerOrder> orders = customerOrderRepository.findAll();
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<CustomerOrder> currentMonthOrders = orders.stream()
                .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(currentMonth))
                .toList();
        List<CustomerOrder> previousMonthOrders = orders.stream()
                .filter(o -> o.getCreatedAt() != null && YearMonth.from(o.getCreatedAt()).equals(previousMonth))
                .toList();

        BigDecimal currentRevenue = sumOrderTotals(currentMonthOrders);
        BigDecimal previousRevenue = sumOrderTotals(previousMonthOrders);
        BigDecimal currentCommission = sumCommissionForOrders(currentMonthOrders);
        BigDecimal previousCommission = sumCommissionForOrders(previousMonthOrders);

        Map<String, Object> current = buildMonthlySummary(currentMonthOrders.size(), currentRevenue, currentCommission);
        Map<String, Object> previous = buildMonthlySummary(previousMonthOrders.size(), previousRevenue, previousCommission);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("revenueGrowth", calculateGrowthPercent(previousRevenue, currentRevenue));
        comparison.put("orderGrowth", calculateGrowthPercent(BigDecimal.valueOf(previousMonthOrders.size()), BigDecimal.valueOf(currentMonthOrders.size())));
        comparison.put("commissionGrowth", calculateGrowthPercent(previousCommission, currentCommission));

        Map<String, Object> report = new HashMap<>();
        report.put("currentMonth", current);
        report.put("previousMonth", previous);
        report.put("comparison", comparison);
        report.put("generatedAt", LocalDateTime.now().toString());
        return report;
    }

    private Map<String, Object> buildMonthlySummary(long orders, BigDecimal revenue, BigDecimal commission) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("orders", orders);
        summary.put("revenue", revenue);
        summary.put("commission", commission);
        summary.put("payout", revenue.subtract(commission));
        return summary;
    }

    private BigDecimal sumOrderTotals(List<CustomerOrder> orders) {
        return orders.stream()
                .map(o -> o.getTotalPrice() == null ? BigDecimal.ZERO : o.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCommissionForOrders(List<CustomerOrder> orders) {
        return orders.stream()
                .map(order -> calculateCommissionAmount(
                        order.getTotalPrice() == null ? BigDecimal.ZERO : order.getTotalPrice(),
                        getSellerCommissionRate(order.getSellerId())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCommissionAmount(BigDecimal amount, BigDecimal commissionRate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = commissionRate == null ? BigDecimal.TEN : commissionRate;
        return amount.multiply(rate).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateGrowthPercent(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getSellerCommissionRate(UUID sellerId) {
        try {
            UserClient.UserSummary seller = userClient.getSellerById(sellerId).getData();
            if (seller != null && seller.commissionRate() != null) {
                return seller.commissionRate();
            }
        } catch (Exception e) {
            System.err.println("Failed to load seller commission rate for " + sellerId + ": " + e.getMessage());
        }
        return BigDecimal.TEN;
    }

    private OrderDto toOrderDto(CustomerOrder order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyerId());
        dto.setSellerId(order.getSellerId());
        dto.setProductId(order.getProductId());
        dto.setProductTitle(order.getProductTitle());
        dto.setQuantity(order.getQuantity());
        dto.setOriginalPrice(order.getOriginalPrice());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDiscountCode(order.getDiscountCode());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setStatus(order.getStatus());
        dto.setDeliveryCity(order.getDeliveryCity());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    private ReviewDto toReviewDto(ProductReview review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
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

    private long getActiveProductCount(UUID sellerId) {
        try {
            var response = productClient.getUserProductsByStatus(sellerId, "ACTIVE");
            if (response == null || response.getData() == null) {
                return 0L;
            }
            return response.getData().size();
        } catch (Exception e) {
            return 0L;
        }
    }
}
