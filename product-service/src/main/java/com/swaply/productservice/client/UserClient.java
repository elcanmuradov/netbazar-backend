package com.swaply.productservice.client;

import com.swaply.productservice.dto.ApiResponse;
import com.swaply.productservice.utils.enums.ProductStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Component
@FeignClient(name = "user-service", url = "http://user-service:8080")
public interface UserClient {

    @GetMapping("/user/favorites")
    List<UUID> findAllFavorites();

    @GetMapping("/seller/profile/{id}")
    ApiResponse<UserSummary> getSellerById(@PathVariable("id") UUID id);



    record UserSummary(UUID id, String email, String name, String userRole, BigDecimal commissionRate, BigDecimal paymentsTotal) {}

}
