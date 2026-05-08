package com.swaply.userservice.exception;

import com.swaply.userservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnexpectedTokenException.class)
    public ResponseEntity<ApiResponse> handleUnexpectedTokenException(UnexpectedTokenException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(TokenBlacklistedException.class)
    public ResponseEntity<ApiResponse> handleTokenBlacklistedException(TokenBlacklistedException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse> handleAuthException(AuthException e){
        return ResponseEntity.status(401).body(ApiResponse.fail(e.getMessage()));
    }

}
