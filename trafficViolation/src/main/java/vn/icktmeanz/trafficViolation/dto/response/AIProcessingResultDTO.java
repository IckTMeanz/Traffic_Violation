package vn.icktmeanz.trafficViolation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIProcessingResultDTO {
    private List<DetectedObjectDTO> objects;

    @JsonProperty("processed_url")
    private String processedUrl;
}
