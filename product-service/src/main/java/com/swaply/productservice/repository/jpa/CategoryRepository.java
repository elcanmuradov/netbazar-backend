package com.swaply.productservice.repository.jpa;

import com.swaply.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    List<Category> findAllByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc();
    List<Category> findAllByParentIdAndIsActiveTrueOrderBySortOrderAsc(UUID parentId);
}
