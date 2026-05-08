package com.swaply.productservice.dto;

import lombok.Builder;
import lombok.Data;

import static com.swaply.productservice.utils.messages.Constants.SUCCESS_MESSAGE;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

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
