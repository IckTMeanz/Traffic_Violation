package vn.icktmeanz.trafficViolation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaFileResponse {
    private Long id;
    private String originalUrl;
    private String aiStatus;
    private String processedUrl;
}
