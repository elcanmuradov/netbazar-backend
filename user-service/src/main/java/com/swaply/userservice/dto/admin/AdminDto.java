package com.swaply.userservice.dto.admin;

import com.swaply.userservice.utils.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminDto {
    private UUID id;

    private String name;

    private String email;

    @Builder.Default
    private UserRole role = UserRole.ADMIN;
}
