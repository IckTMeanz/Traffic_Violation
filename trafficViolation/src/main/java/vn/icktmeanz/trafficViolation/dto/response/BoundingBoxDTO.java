package vn.icktmeanz.trafficViolation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoundingBoxDTO {
    private Integer xmin;
    private Integer ymin;
    private Integer xmax;
    private Integer ymax;
}
