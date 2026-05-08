package com.swaply.userservice.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class RegisterAdminRequest {
    private String name;
    @Size(min = 8, max = 50)
    private String email;
    @Size(min = 8, max = 50)
    private String password;

}
