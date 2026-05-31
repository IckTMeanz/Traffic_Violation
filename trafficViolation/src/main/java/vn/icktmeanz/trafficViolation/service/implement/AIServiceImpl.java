package vn.icktmeanz.trafficViolation.service.implement;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.icktmeanz.trafficViolation.dto.response.AIProcessingResultDTO;
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
    public AIProcessingResultDTO processImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                throw new IllegalArgumentException("Image file not found: " + imagePath);
            }
            log.info("Processing image via AI service: {}", imagePath);
            return callAIServiceForSingleImage(imageFile);
        } catch (Exception e) {
            log.error("Error processing image: {}", imagePath, e);
            throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
        }
    }

    private AIProcessingResultDTO callAIServiceForSingleImage(File imageFile) {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/process-image";
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(imageFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), AIProcessingResultDTO.class);
            } else {
                throw new RuntimeException("AI service returned error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling AI service for single image", e);
            throw new RuntimeException("Failed to call AI service: " + e.getMessage(), e);
        }
    }

    /**
     * SỬA LỖI: Nhận về danh sách List<AIProcessingResultDTO> vì Flask trả về mảng kết quả cho Folder
     */
    public List<AIProcessingResultDTO> processFolder(File[] imageFiles) {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/process-folder";
            log.info("Calling folder processing AI service: {}", serviceUrl);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            for (File imageFile : imageFiles) {
                body.add("files", new FileSystemResource(imageFile));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serviceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Folder processing AI service response received successfully");
                // Đọc dữ liệu dưới dạng List thay vì Object đơn lẻ
                return objectMapper.readValue(response.getBody(), new TypeReference<List<AIProcessingResultDTO>>() {});
            } else {
                throw new RuntimeException("AI service returned error status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling folder processing AI service", e);
            throw new RuntimeException("Failed to call AI service: " + e.getMessage(), e);
        }
    }

    public AIProcessingResultDTO processVideo(File videoFile) {
        try {
            String serviceUrl = aiServiceUrl + "/api/ai/process-video";
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(videoFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
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
}