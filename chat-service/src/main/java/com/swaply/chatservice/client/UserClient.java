package com.swaply.chatservice.client;


import com.swaply.chatservice.dto.ApiResponse;
import com.swaply.chatservice.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url:http://user-service:8080}")
public interface UserClient {

    @GetMapping("/profile")
    ApiResponse<UserDto> getUserById(@RequestParam("id") UUID id, @RequestHeader(value = "Authorization", required = false) String token);

}
