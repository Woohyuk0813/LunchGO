package com.example.LunchGo.image.controller;

import com.example.LunchGo.image.dto.ImageUploadResponse;
import com.example.LunchGo.image.dto.PresignedUrlResponse;
import com.example.LunchGo.image.service.ObjectStorageService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageUploadController {

    private final ObjectStorageService objectStorageService;

    @PostMapping("/upload/{domain}")
    public ResponseEntity<ImageUploadResponse> upload(
        @PathVariable String domain,
        @RequestParam("file") MultipartFile file
    ) {
        ImageUploadResponse response = objectStorageService.upload(domain, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presign")
    public ResponseEntity<PresignedUrlResponse> presign(
        @RequestParam("key") String key,
        @RequestParam(value = "expiresSeconds", defaultValue = "300") long expiresSeconds
    ) {
        if (!key.startsWith("receipts/")) {
            return ResponseEntity.badRequest().build();
        }
        long safeSeconds = Math.min(Math.max(expiresSeconds, 60), 900);
        String url = objectStorageService.createPresignedUrl(key, Duration.ofSeconds(safeSeconds));
        return ResponseEntity.ok(new PresignedUrlResponse(url));
    }
}
