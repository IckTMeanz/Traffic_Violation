package vn.icktmeanz.trafficViolation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFeedbackRequest {
    private Long uploadSessionId;
    private String feedbackContent;
}
