package com.swaply.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import com.swaply.productservice.dto.ApiResponse;

@FeignClient(name = "media-service", url = "http://media-service:8080")
public interface MediaClient {
    @PostMapping(path = "/media/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<List<Map<String, String>>> uploadMultipleFiles(@RequestPart("files") List<MultipartFile> files);


    @DeleteMapping("/media/delete-images")
    void deleteImages(@RequestParam("publicIds") List<String> publicIds);
}