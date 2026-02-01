package com.example.LunchGo.image.service;

import com.example.LunchGo.image.config.ObjectStorageProperties;
import com.example.LunchGo.image.dto.ImageUploadResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ObjectStorageProperties properties;

    public ImageUploadResponse upload(String domain, MultipartFile file) {
        validateUpload(file);

        String contentType = resolveContentType(file);
        String extension = resolveExtension(file, contentType);
        String key = buildKey(domain, extension);

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .contentType(contentType);

        if (isPublicDomain(domain) && properties.isPublicRead()) {
            requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
        }

        try {
            s3Client.putObject(
                requestBuilder.build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new IllegalStateException("failed to upload file", e);
        }

        String fileUrl = null;
        if (isPublicDomain(domain)) {
            fileUrl = buildPublicUrlInternal(key);
        }
        return new ImageUploadResponse(fileUrl, key, contentType, file.getSize());
    }

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }
        String contentType = resolveContentType(file);
        validateFile(file, contentType);
    }

    public String createPresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(duration)
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public void deleteObject(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .build());
    }

    private String buildKey(String domain, String extension) {
        LocalDate today = LocalDate.now();
        String year = YEAR_FORMAT.format(today);
        String month = MONTH_FORMAT.format(today);
        String day = DAY_FORMAT.format(today);
        String uuid = UUID.randomUUID().toString();

        return switch (domain.toLowerCase(Locale.ROOT)) {
            case "reviews" -> String.format("reviews/%s/%s/%s.%s", year, month, uuid, extension);
            case "receipts" -> String.format("receipts/%s/%s/%s.%s", year, month, uuid, extension);
            case "cafeteria" -> String.format("cafeteria/%s/%s/%s/%s.%s", year, month, day, uuid, extension);
            case "restaurants" -> String.format("restaurants/%s/%s/%s.%s", year, month, uuid, extension);
            case "menus" -> String.format("menus/%s/%s/%s.%s", year, month, uuid, extension);
            case "profile" -> String.format("profile/%s/%s/%s/%s.%s", year, month, day, uuid, extension);
            default -> throw new IllegalArgumentException("unsupported domain: " + domain);
        };
    }

    private boolean isPublicDomain(String domain) {
        String normalized = domain.toLowerCase(Locale.ROOT);
        return "reviews".equals(normalized)
            || "cafeteria".equals(normalized)
            || "restaurants".equals(normalized)
            || "menus".equals(normalized)
            || "profile".equals(normalized);
    }

    private String buildPublicUrlInternal(String key) {
        return properties.getEndpoint() + "/" + properties.getBucket() + "/" + key;
    }

    private String resolveExtension(MultipartFile file, String contentType) {
        String original = file.getOriginalFilename();
        if (original != null) {
            int idx = original.lastIndexOf('.');
            if (idx > -1 && idx + 1 < original.length()) {
                return original.substring(idx + 1).toLowerCase(Locale.ROOT);
            }
        }

        if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            return "jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            return "png";
        }
        if (MediaType.IMAGE_GIF_VALUE.equals(contentType)) {
            return "gif";
        }
        if ("image/webp".equals(contentType)) {
            return "webp";
        }

        return "bin";
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return contentType;
    }

    private void validateFile(MultipartFile file, String contentType) {
        long maxSize = properties.getMaxSizeBytes();
        if (maxSize > 0 && file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file exceeds max size");
        }

        List<String> allowedTypes = properties.getAllowedContentTypes();
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            boolean allowed = allowedTypes.stream()
                .anyMatch(type -> type.equalsIgnoreCase(contentType));
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported content type");
            }
        }
    }

    public String normalizeKey(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return null;
        }
        String prefix = properties.getEndpoint() + "/" + properties.getBucket() + "/";
        if (storedValue.startsWith(prefix)) {
            return storedValue.substring(prefix.length());
        }
        return storedValue;
    }
}
