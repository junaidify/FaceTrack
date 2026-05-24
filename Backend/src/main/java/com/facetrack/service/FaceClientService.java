package com.facetrack.service;

import com.facetrack.config.AppProperties;
import com.facetrack.exception.FaceServiceException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FaceClientService {

    private static final Pattern DATA_URL_RE = Pattern.compile("^data:image/\\w+;base64,");

    private final WebClient webClient;
    private final Duration timeout;

    public FaceClientService(AppProperties props) {
        String baseUrl = props.getFaceService().getUrl().replaceAll("/+$", "");
        this.timeout = Duration.ofSeconds(props.getFaceService().getTimeoutSeconds());
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    private String cleanB64(String image) {
        return DATA_URL_RE.matcher(image).replaceFirst("").trim();
    }

    public List<Double> embed(String imageB64) {
        Map<String, Object> payload = Map.of("image", cleanB64(imageB64));

        EmbedResponse response = webClient.post()
                .uri("/embed")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() == 422,
                        clientResponse -> clientResponse.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    String msg = body.containsKey("detail")
                                            ? body.get("detail").toString()
                                            : "No face detected.";
                                    return Mono.error(new FaceServiceException(msg, HttpStatus.BAD_REQUEST));
                                }))
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> Mono.error(new FaceServiceException(
                                "FaceService /embed failed: " + clientResponse.statusCode())))
                .bodyToMono(EmbedResponse.class)
                .block(timeout);

        if (response == null || response.getEmbedding() == null) {
            throw new FaceServiceException("FaceService returned empty embedding.");
        }
        return response.getEmbedding();
    }

    public MatchResult match(String imageB64, List<List<Double>> candidates, double threshold) {
        Map<String, Object> payload = Map.of(
                "image", cleanB64(imageB64),
                "candidates", candidates,
                "threshold", threshold
        );

        MatchResult response = webClient.post()
                .uri("/match")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() == 422,
                        clientResponse -> clientResponse.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    String msg = body.containsKey("detail")
                                            ? body.get("detail").toString()
                                            : "No face detected.";
                                    return Mono.error(new FaceServiceException(msg, HttpStatus.BAD_REQUEST));
                                }))
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> Mono.error(new FaceServiceException(
                                "FaceService /match failed: " + clientResponse.statusCode())))
                .bodyToMono(MatchResult.class)
                .block(timeout);

        if (response == null) {
            throw new FaceServiceException("FaceService returned null match result.");
        }
        return response;
    }

    @Data
    public static class EmbedResponse {
        private List<Double> embedding;
    }

    @Data
    public static class MatchResult {
        private boolean matched;
        @JsonProperty("best_index")
        private Integer bestIndex;
        private Double similarity;
    }
}
