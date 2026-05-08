package com.swaply.userservice.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginAdminRequest {
    private String email;

    private String password;
}
