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
public class DetectedObjectDTO {
    @JsonProperty("object_id")

    private String objectId;

    @JsonProperty("violation_types")
    private List<String> violationTypes;

    @JsonProperty("bbox")
    private BoundingBoxDTO boundingBox;

    private Double confidence;

    @JsonProperty("frame_number")
    private Integer frameNumber;
}