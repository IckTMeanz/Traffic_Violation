package vn.icktmeanz.trafficViolation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class JsonUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("Error converting object to JSON", e);
            throw new RuntimeException("Failed to convert to JSON: " + e.getMessage(), e);
        }
    }
    
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            log.error("Error parsing JSON: {}", json, e);
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
}
