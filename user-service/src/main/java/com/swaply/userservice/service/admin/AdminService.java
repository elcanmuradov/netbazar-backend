package com.swaply.userservice.service.admin;

import com.swaply.userservice.client.ChatClient;
import com.swaply.userservice.client.MediaClient;
import com.swaply.userservice.client.ProductClient;
import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.admin.DashboardStatsDto;
import com.swaply.userservice.dto.admin.message.ReportMessageDto;
import com.swaply.userservice.dto.user.UserDto;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.entity.User;
import com.swaply.userservice.exception.AuthException;
import com.swaply.userservice.mapper.UserMapper;
import com.swaply.userservice.repository.SellerRepository;
import com.swaply.userservice.repository.UserRepository;
import com.swaply.userservice.utils.enums.AccountStatus;
import com.swaply.userservice.utils.enums.MessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final ChatClient chatClient;
    private final ProductClient productClient;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final UserMapper userMapper;
    private final MediaClient mediaClient;

    // ─── KPI ────────────────────────────────────────────────────────────────

    public Long getProductCount() {
        Long count = productClient.getProductCount().getData();
        log.info("Product count : {}", count);
        return count;
    }

    public Long getUserCount() {
        Long count = userRepository.count();
        log.info("User count : {}", count);
        return count;
    }

    public DashboardStatsDto getDashboardStats() {
        Long products = getProductCount();
        Long users = getUserCount();
        List<ReportMessageDto> reports = getReportedMessages();
        long activeReports = reports.stream()
                .filter(m -> m.getStatus().equals(MessageStatus.PENDING))
                .count();
        long totalSellers = sellerRepository.count();

        return DashboardStatsDto.builder()
                .totalProducts(products)
                .totalUsers(users)
                .totalReports(reports.size())
                .activeReports(activeReports)
                .totalSellers(totalSellers)
                .productGrowth(12.5)
                .userGrowth(8.2)
                .build();
    }

    public void banUser(UUID userId, Long seconds) {
        User user = userRepository.getUserById(userId);
        user.setStatus(AccountStatus.BANNED);
        user.setExpiredAt(LocalDateTime.now().plusSeconds(seconds));
        userRepository.save(user);
    }

    public void unBanUser(UUID userId) {
        User user = userRepository.getUserById(userId);
        user.setStatus(AccountStatus.ACTIVE);
        user.setExpiredAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<UserDto> getUsers() {
        List<UserDto> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> users.add(userMapper.entityToDto(user)));
        return users;
    }

    // ─── SELLER MANAGEMENT ──────────────────────────────────────────────────

    public List<Seller> getAllSellers() {
        List<Seller> sellers = sellerRepository.findAll();
        for (Seller seller : sellers) {
            enrichSellerWithMonthlyStats(seller);
        }
        sellers.sort(Comparator.comparing(Seller::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return sellers;
    }

    public Seller getSellerById(UUID sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AuthException("Seller not found: " + sellerId));
        enrichSellerWithMonthlyStats(seller);
        return seller;
    }

    public void activateSeller(UUID sellerId) {
        Seller seller = getSellerById(sellerId);
        seller.setStatus(AccountStatus.ACTIVE);
        sellerRepository.save(seller);
        log.info("Seller {} activated", sellerId);
    }

    public void deactivateSeller(UUID sellerId) {
        Seller seller = getSellerById(sellerId);
        seller.setStatus(AccountStatus.DEACTIVATED);
        sellerRepository.save(seller);
        log.info("Seller {} deactivated", sellerId);
    }

    public Seller updateSeller(UUID sellerId, Seller updated) {
        Seller seller = getSellerById(sellerId);
        if (updated.getName() != null) seller.setName(updated.getName());
        if (updated.getProfileImageUrl() != null) seller.setProfileImageUrl(updated.getProfileImageUrl());
        if (updated.getBannerImageUrl() != null) seller.setBannerImageUrl(updated.getBannerImageUrl());
        return sellerRepository.save(seller);
    }

    public void penalizeSeller(UUID sellerId, String reason) {
        Seller seller = getSellerById(sellerId);
        seller.setStatus(AccountStatus.PENALIZED);
        seller.setPenalizeReason(reason);
        sellerRepository.save(seller);
        log.info("Seller {} penalized: {}", sellerId, reason);
    }

    // ─── COMMISSION ─────────────────────────────────────────────────────────

    public void setSellerCommission(UUID sellerId, BigDecimal commissionRate) {
        Seller seller = getSellerById(sellerId);
        seller.setCommissionRate(commissionRate);
        sellerRepository.save(seller);
        log.info("Commission rate set to {} for seller {}", commissionRate, sellerId);
    }

    // ─── PAYOUT ─────────────────────────────────────────────────────────────

    public Map<String, Object> processPayout(UUID sellerId, BigDecimal amount) {
        Seller seller = getSellerById(sellerId);
        BigDecimal current = seller.getPaymentsTotal() != null ? seller.getPaymentsTotal() : BigDecimal.ZERO;
        BigDecimal commission = amount.multiply(
                seller.getCommissionRate() != null ? seller.getCommissionRate() : new BigDecimal("10"))
                .divide(BigDecimal.valueOf(100));
        BigDecimal netPayout = amount.subtract(commission);
        seller.setPaymentsTotal(current.add(amount));
        sellerRepository.save(seller);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sellerId", sellerId);
        result.put("grossAmount", amount);
        result.put("commission", commission);
        result.put("netPayout", netPayout);
        result.put("processedAt", LocalDateTime.now().toString());
        log.info("Payout processed for seller {}: net={}", sellerId, netPayout);
        return result;
    }

    public List<Map<String, Object>> getPayoutHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        sellerRepository.findAll().forEach(seller -> {
            enrichSellerWithMonthlyStats(seller);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("sellerId", seller.getId());
            entry.put("sellerName", seller.getName());
            entry.put("totalPaid", seller.getPaymentsTotal());
            entry.put("commissionRate", seller.getCommissionRate());
            entry.put("monthlyOrders", seller.getMonthlyOrders());
            entry.put("monthlyRevenue", seller.getMonthlyRevenue());
            entry.put("monthlyCommissionDue", seller.getMonthlyCommissionDue());
            entry.put("monthlyNetPayout", seller.getMonthlyNetPayout());
            history.add(entry);
        });
        return history;
    }

    public Map<String, Object> getMonthlyReport() {
        return productClient.getMonthlyReport().getData();
    }

    // ─── REPORTED MESSAGES ──────────────────────────────────────────────────

    public List<ReportMessageDto> getReportedMessages() {
        ArrayList<ReportMessageDto> reportMessages = new ArrayList<>();
        var messages = chatClient.getReportMessages().getData();
        messages.forEach(message -> {
            User user = userRepository.findById(message.getSenderId()).orElse(null);
            message.setUser(user != null ? user.getEmail() : "unknown-user");
            reportMessages.add(message);
        });
        log.info("Reported messages : {}", reportMessages);
        return reportMessages;
    }

    public void setBanned(String messageId) {
        chatClient.setBanned(messageId);
    }

    public void setResolved(String messageId) {
        chatClient.setResolved(messageId);
    }

    // ─── MISC ───────────────────────────────────────────────────────────────

    public void deleteAllResources() {
        mediaClient.deleteAll();
    }

    private void enrichSellerWithMonthlyStats(Seller seller) {
        try {
            @SuppressWarnings("unchecked")
            ApiResponse<Map<String, Object>> statsResponse = (ApiResponse<Map<String, Object>>) productClient.getSellerStats(seller.getId());
            Map<String, Object> stats = statsResponse != null ? statsResponse.getData() : null;
            if (stats == null) {
                return;
            }

            BigDecimal monthlyRevenue = toBigDecimal(stats.get("monthlyRevenue"));
            BigDecimal commissionRate = seller.getCommissionRate() != null ? seller.getCommissionRate() : BigDecimal.TEN;
            BigDecimal monthlyCommissionDue = monthlyRevenue.multiply(commissionRate).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

            seller.setMonthlyOrders(toLong(stats.get("monthlyOrders")));
            seller.setMonthlyRevenue(monthlyRevenue);
            seller.setMonthlyCommissionDue(monthlyCommissionDue);
            seller.setMonthlyNetPayout(monthlyRevenue.subtract(monthlyCommissionDue));
        } catch (Exception e) {
            log.warn("Could not enrich seller {} with monthly stats: {}", seller.getId(), e.getMessage());
            seller.setMonthlyOrders(0L);
            seller.setMonthlyRevenue(BigDecimal.ZERO);
            seller.setMonthlyCommissionDue(BigDecimal.ZERO);
            seller.setMonthlyNetPayout(BigDecimal.ZERO);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }
}
