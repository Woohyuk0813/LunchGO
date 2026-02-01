package com.example.LunchGo.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadResponse {
    private String fileUrl;
    private String key;
    private String contentType;
    private long size;
}
