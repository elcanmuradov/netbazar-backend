package com.swaply.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

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
