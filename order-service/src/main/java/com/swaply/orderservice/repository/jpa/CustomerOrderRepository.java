package com.swaply.orderservice.repository.jpa;

import com.swaply.orderservice.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    List<CustomerOrder> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
}
