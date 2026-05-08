package com.swaply.productservice.repository.jpa;

import com.swaply.productservice.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {
    List<Banner> findAllByPlacementAndIsActiveTrueOrderBySortOrderAsc(String placement);
    List<Banner> findAllByOrderBySortOrderAsc();
}
