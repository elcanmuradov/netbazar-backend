package com.swaply.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swaply.userservice.utils.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sellers")
public class Seller implements UserDetails {
    @JsonProperty("userRole")
    public String getUserRole() {
        return "SELLER";
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private String profileImageUrl = "https://static.vecteezy.com/system/resources/previews/007/335/692/non_2x/account-icon-template-vector.jpg";

    private String bannerImageUrl;

    private Long productCount;

    private BigDecimal paymentsTotal;

    @Transient
    private Long monthlyOrders;

    @Transient
    private BigDecimal monthlyRevenue;

    @Transient
    private BigDecimal monthlyCommissionDue;

    @Transient
    private BigDecimal monthlyNetPayout;

    @Builder.Default
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("10.00"); // default 10%

    @Column(name = "penalize_reason")
    private String penalizeReason;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @LastModifiedDate
    private LocalDateTime updatedAt;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("SELLER"));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
