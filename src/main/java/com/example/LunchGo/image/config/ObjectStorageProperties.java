package com.example.LunchGo.image.config;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ncp.object-storage")
public class ObjectStorageProperties {
    private String endpoint;
    private String region;
    private String bucket;
    private boolean publicRead;
    private String accessKey;
    private String secretKey;
    private long maxSizeBytes;
    private List<String> allowedContentTypes;
}
