package com.swaply.userservice.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderDto {
    private String id;
    private String customer;
    private String status;
    private String payment;
    private String delivery;
    private String createdAt;
}
