package com.swaply.mediaservice.controller;

import com.swaply.mediaservice.dto.ApiResponse;
import com.swaply.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class MediaController {
    private final MediaService mediaService;


    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> uploadMultipleFiles(
            @RequestPart("files") List<MultipartFile> files) {

        return ResponseEntity.ok(ApiResponse.success(mediaService.uploadFiles(files)));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.uploadFile(file)));
    }

    @PostMapping("/signature")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSignature(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.getUploadSignature(params)));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<Void>> deleteAll(){
        try {
            mediaService.deleteAllResources();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/delete-images")
    public void deleteImages(@RequestParam("publicIds") List<String> publicIds){
        mediaService.deleteImages(publicIds);
    }

}
