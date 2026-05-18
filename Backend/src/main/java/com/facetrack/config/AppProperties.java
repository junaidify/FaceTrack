package com.facetrack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private FaceService faceService = new FaceService();
    private Cors cors = new Cors();
    private Seed seed = new Seed();

    @Data
    public static class Jwt {
        private String secret = "changeme-min-16-chars";
        private String algorithm = "HS256";
        private int expirationMinutes = 720;
    }

    @Data
    public static class FaceService {
        private String url = "http://face-service:8002";
        private double matchThreshold = 0.45;
        private int timeoutSeconds = 30;
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "*";
    }

    @Data
    public static class Seed {
        private String adminEmail = "admin@facetrack.local";
        private String adminPassword = "admin123";
        private String adminName = "Default Admin";
    }
}
