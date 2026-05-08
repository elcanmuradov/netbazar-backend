package com.swaply.userservice.dto.user.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Size(min = 8, max = 100)
    @Email
    private String email;

    @Size(min = 8, max = 255)
    private String password;
}
