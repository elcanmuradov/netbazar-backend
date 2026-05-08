package com.swaply.userservice.dto.user;

import com.swaply.userservice.utils.enums.AccountStatus;
import com.swaply.userservice.utils.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;

    private String name;

    private String email;

    private String phone;

    private UserRole userRole;

    private AccountStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    private LocalDateTime expiredAt;

    private String profileImageUrl;

    @Builder.Default
    private List<UUID> favoritedProductsIds = new ArrayList<>();

}
