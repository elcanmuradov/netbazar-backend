package com.swaply.productservice.repository.jpa;

import com.swaply.productservice.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    Optional<BlogPost> findBySlug(String slug);
    Page<BlogPost> findAllByStatus(String status, Pageable pageable);
    Page<BlogPost> findAllByCategoryName(String categoryName, Pageable pageable);
}
