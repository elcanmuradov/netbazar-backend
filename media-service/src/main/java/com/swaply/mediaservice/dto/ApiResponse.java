package com.swaply.mediaservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T>{
    private  boolean success;
    private  String message;
    private  T data;

    public static  <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).message("SUCCESS").data(data).build();
    }

    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}
