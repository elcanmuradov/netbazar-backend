package com.swaply.orderservice.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.swaply.orderservice.dto.ApiResponse;

@Component
@FeignClient(name = "user-service", url = "http://user-service:8080")
public interface UserClient {

    @GetMapping("/user/favorites")
    List<UUID> findAllFavorites();

    @GetMapping("/seller/profile/{id}")
    ApiResponse<UserSummary> getSellerById(@PathVariable("id") UUID id);



    record UserSummary(UUID id, String email, String name, String userRole, BigDecimal commissionRate, BigDecimal paymentsTotal) {}

}

