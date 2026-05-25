package vn.icktmeanz.trafficViolation.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.constant.UploadType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UploadSessionResponse {
    private Long sessionId;
    private UploadType uploadType;
    private SessionStatus status;
    private String videoUrl;
    private LocalDateTime createdAt;
    private List<MediaFileResponse> mediaFiles;
}
