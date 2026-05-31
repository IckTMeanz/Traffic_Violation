package vn.icktmeanz.trafficViolation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long id;
    private Long uploadSessionId;
    private String userFullName;
    private String userPhone;
    private String description;
    private FeedbackStatus status;
    private String handledByName;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
