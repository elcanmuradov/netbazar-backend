package com.swaply.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponse<T> {

    private static final String SUCCESS_MESSAGE = "SUCCESS";
    private  boolean success;
    private  String message;
    private  T data;

    public static  <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).message(SUCCESS_MESSAGE).data(data).build();
    }

    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }

}
