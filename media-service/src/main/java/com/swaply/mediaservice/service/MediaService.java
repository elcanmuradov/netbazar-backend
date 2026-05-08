package com.swaply.mediaservice.service;


//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final Cloudinary cloudinary;

    public MediaService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> getUploadSignature(Map<String, Object> params) {
        long timestamp = System.currentTimeMillis() / 1000L;
        params.put("timestamp", timestamp);

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        Map<String, Object> response = new HashMap<>();
        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("api_key", cloudinary.config.apiKey);
        response.put("cloud_name", cloudinary.config.cloudName);

        return response;
    }

    public List<Map<String, String>> uploadFiles(List<MultipartFile> files) {
        log.info("Uploading files to Cloudinary");
        List<Map<String, String>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (!file.isEmpty()) {
                    Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                    results.add(Map.of(
                            "url", String.valueOf(uploadResult.get("secure_url")),
                            "publicId", String.valueOf(uploadResult.get("public_id"))
                    ));
                }
            } catch (IOException e) {
                results.add(Map.of(
                        "error", "Fayl yüklənmədi: " + file.getOriginalFilename(),
                        "fileName", file.getOriginalFilename()
                ));
            }
        }

        log.info("Uploaded files to Cloudinary");
        return results;
    }

    public Map<String, String> uploadFile(MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                return Map.of(
                        "url", String.valueOf(uploadResult.get("secure_url")),
                        "publicId", String.valueOf(uploadResult.get("public_id"))
                );
            }
        } catch (IOException e) {
            log.error("Fayl yüklənmədi: " + file.getOriginalFilename(), e);
        }
        return null;
    }


    public void deleteAllResources() {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("max_results", 100);
            options.put("resource_type", "image");

            String nextCursor = null;

            do {
                if (nextCursor != null) {
                    options.put("next_cursor", nextCursor);
                }

                ApiResponse resources = cloudinary.api().resources(options);
                Object resourceObject = resources.get("resources");

                if (!(resourceObject instanceof List<?> resourceList)) {
                    break;
                }

                if (resourceList.isEmpty()) {
                    break;
                }

                List<String> publicIds = new ArrayList<>();
                for (Object resource : resourceList) {
                    Object publicId = ((Map<?, ?>) resource).get("public_id");
                    publicIds.add(String.valueOf(publicId));
                }

                cloudinary.api().deleteResources(publicIds, options);

                log.info("Silindi: " + publicIds.size() + " ədəd");

                nextCursor = (String) resources.get("next_cursor");

            } while (nextCursor != null);

            log.info("Bütün resurslar silindi!");
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    public void deleteImages(List<String> publicIds) {
        try {
            if (publicIds != null && !publicIds.isEmpty()) {
                cloudinary.api().deleteResources(publicIds, ObjectUtils.emptyMap());
                log.info("Cloudinary-dən silindi: " + publicIds.size() + " ədəd şəkil.");
            }
        } catch (Exception e) {
            log.error("Resurslar silinərkən xəta: " + e.getMessage());
        }
    }
}