package com.swaply.userservice.client;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.config.MediaFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "media-service", url = "http://media-service:8080", configuration = MediaFeignConfig.class)
@Component
public interface MediaClient {
    @GetMapping("/media/delete-all")
    ApiResponse<Long> deleteAll();

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<Map<String, String>> upload(@RequestPart("file") MultipartFile file);
}