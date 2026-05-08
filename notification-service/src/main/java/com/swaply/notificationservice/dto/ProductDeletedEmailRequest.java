package com.swaply.notificationservice.dto;

public class ProductDeletedEmailRequest {
    public String email;
    public String productTitle;
    public String reason;

    public ProductDeletedEmailRequest() {
    }

    public ProductDeletedEmailRequest(String email, String productTitle, String reason) {
        this.email = email;
        this.productTitle = productTitle;
        this.reason = reason;
    }
}
