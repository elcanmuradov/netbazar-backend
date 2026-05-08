package com.swaply.userservice.repository;

import com.swaply.userservice.entity.Seller;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findByPhone(@Size(min = 10, max = 10) String phone);

    Optional<Seller> findByEmail(String email);

    Optional<Seller> findByUsername(String username);

    @Query("SELECT s FROM Seller s WHERE LOWER(s.username) LIKE LOWER(CONCAT('%', :pattern, '%')) ORDER BY s.username ASC")
    List<Seller> searchByUsernamePattern(String pattern);

}
