package com.swaply.orderservice.controller;

import com.swaply.orderservice.dto.ApiResponse;
import com.swaply.orderservice.dto.campaign.CampaignDto;
import com.swaply.orderservice.entity.CustomerOrder;
import com.swaply.orderservice.exception.NotFoundException;
import com.swaply.orderservice.repository.jpa.CustomerOrderRepository;
import com.swaply.orderservice.service.CampaignService;
import com.swaply.orderservice.service.CommerceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final CustomerOrderRepository customerOrderRepository;
    private final CampaignService campaignService;
    private final CommerceService commerceService;

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
            @PathVariable UUID orderId,
            @RequestParam String status) {
        commerceService.updateOrderStatus(orderId, status);
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

    @GetMapping("/orders/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> orderSummary() {
        List<CustomerOrder> orders = customerOrderRepository.findAll();
        long delivered = orders.stream().filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus())).count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", orders.size());
        data.put("delivered", delivered);
        data.put("monthly", commerceService.getMonthlyReport());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
