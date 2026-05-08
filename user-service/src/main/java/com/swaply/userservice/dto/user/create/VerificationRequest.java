package com.swaply.userservice.dto.user.create;

import lombok.Data;

@Data
public class VerificationRequest {
    private String email;
    private String token;
}
