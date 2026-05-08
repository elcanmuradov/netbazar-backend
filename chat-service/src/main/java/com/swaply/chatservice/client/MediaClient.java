package com.swaply.chatservice.client;

import com.swaply.chatservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@FeignClient(name = "media-service", url = "http://media-service:8080")
public interface MediaClient {

    @PostMapping(path = "/media/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<List<Map<String, String>>> uploadMultipleFiles(@RequestPart("files") List<MultipartFile> files);
}
