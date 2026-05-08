package com.swaply.notificationservice.dto;


public class VerificationRequest {
    public String email;
    public String token;
    public VerificationRequest(String email, String token) {
        this.email = email;
        this.token = token;
    }
}
