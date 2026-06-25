package vn.icktmeanz.trafficViolation.service.implement;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.icktmeanz.trafficViolation.dto.response.AIProcessingResultDTO;
import vn.icktmeanz.trafficViolation.dto.response.AIRetrainStatusResponse;
import vn.icktmeanz.trafficViolation.service.AIService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.ai.service.url}")
    private String aiServiceUrl;

    @Override
    public AIProcessingResultDTO processImage(String imageUrl) {
        try {
            log.info("Calling AI service for single image URL: {}", imageUrl);
            String serviceUrl = aiServiceUrl + "/api/ai/process-image";

            // Gửi Body dạng JSON { "url": "..." }
            Map<String, String> requestBody = Map.of("url", imageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), AIProcessingResultDTO.class);
            } else {
                throw new RuntimeException("AI service returned error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error processing image URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to process image via URL: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<AIProcessingResultDTO> processFolder(List<String> imageUrls) {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/process-folder";
            log.info("Calling batch folder processing AI service with {} URLs", imageUrls.size());

            // Gửi Body dạng JSON { "urls": ["...", "..."] }
            Map<String, List<String>> requestBody = Map.of("urls", imageUrls);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, List<String>>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<List<AIProcessingResultDTO>>() {});
            } else {
                throw new RuntimeException("AI service returned error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling folder processing AI service", e);
            throw new RuntimeException("Failed to call AI service: " + e.getMessage(), e);
        }
    }

    @Override
    public AIProcessingResultDTO processVideo(String videoUrl) {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/process-video";
            log.info("Calling video processing AI service for URL: {}", videoUrl);

            // Gửi Body dạng JSON { "url": "..." }
            Map<String, String> requestBody = Map.of("url", videoUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), AIProcessingResultDTO.class);
            } else {
                throw new RuntimeException("AI service returned error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling video processing AI service", e);
            throw new RuntimeException("Failed to call AI service: " + e.getMessage(), e);
        }
    }

    @Override
    public String retrainModel() {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/retrain";
            log.info("Calling AI retrain service: {}", serviceUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(Map.of(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }

            throw new RuntimeException("AI service returned status: " + response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.warn("AI retrain service returned error status: {}", e.getStatusCode());

            if (e.getStatusCode().value() == 409) {
                throw new IllegalStateException("Model is already retraining");
            }

            throw new RuntimeException("Failed to retrain AI model: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error calling AI retrain service", e);
            throw new RuntimeException("Failed to retrain AI model: " + e.getMessage(), e);
        }
    }

    @Override
    public AIRetrainStatusResponse getRetrainStatus() {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/retrain/status";
            log.info("Calling AI retrain status service: {}", serviceUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), AIRetrainStatusResponse.class);
            }

            throw new RuntimeException("AI service returned status: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error getting AI retrain status", e);
            throw new RuntimeException("Failed to get retrain status: " + e.getMessage(), e);
        }
    }
}