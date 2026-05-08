package com.swaply.productservice.repository.jpa;

import com.swaply.productservice.entity.DiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRuleRepository extends JpaRepository<DiscountRule, UUID> {
    @Query("SELECT d FROM DiscountRule d WHERE UPPER(d.code) = UPPER(:code) AND d.isActive = true")
    Optional<DiscountRule> findByCodeAndIsActiveTrue(String code);
    
    List<DiscountRule> findAllBySellerId(UUID sellerId);
    List<DiscountRule> findAllBySellerIdIsNull(); // Platform discounts
}
