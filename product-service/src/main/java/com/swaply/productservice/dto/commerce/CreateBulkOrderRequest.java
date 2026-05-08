package com.swaply.productservice.dto.commerce;

import java.util.List;

public class CreateBulkOrderRequest {

    private List<BulkOrderItemRequest> items;
    private String paymentMethod;
    private String deliveryCity;
    private String discountCode;

    public List<BulkOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BulkOrderItemRequest> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }
}
