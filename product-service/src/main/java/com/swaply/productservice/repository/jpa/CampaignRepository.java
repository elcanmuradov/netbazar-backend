package com.swaply.productservice.repository.jpa;

import com.swaply.productservice.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    List<Campaign> findAllByIsActiveTrueOrderByStartDateDesc();
}
