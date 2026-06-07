package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;
import vn.icktmeanz.trafficViolation.dto.request.CreateFeedbackRequest;
import vn.icktmeanz.trafficViolation.dto.response.FeedbackResponse;
import vn.icktmeanz.trafficViolation.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackApiController {
    private final FeedbackService feedbackService;

    /**
     * Create new feedback/appeal for an upload session
     * POST /api/feedback
     *
     * @param request CreateFeedbackRequest with uploadSessionId and feedbackContent
     * @return created feedback response
     */
    @PostMapping("/create")
    public ResponseEntity<FeedbackResponse> createFeedback(@RequestBody CreateFeedbackRequest request) {
        log.info("Received feedback creation request for session: {}", request.getUploadSessionId());
        try {
            FeedbackResponse response = feedbackService.createFeedback(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid feedback request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get feedback by ID
     * GET /api/feedback/{id}
     *
     * @param id feedback ID
     * @return feedback response
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long id) {
        log.info("Fetching feedback: {}", id);
        try {
            FeedbackResponse response = feedbackService.getFeedbackById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Feedback not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get feedback by upload session ID
     * GET /api/feedback/session/{sessionId}
     *
     * @param sessionId upload session ID
     * @return feedback response
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<FeedbackResponse> getFeedbackBySession(@PathVariable Long sessionId) {
        log.info("Fetching feedback for session: {}", sessionId);
        try {
            FeedbackResponse response = feedbackService.getFeedbackBySessionId(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Feedback not found for session: {}", sessionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all pending feedback (for authority review)
     * GET /api/feedback/pending/all
     *
     * @return list of pending feedback
     */
    @GetMapping("/pending/all")
    public ResponseEntity<List<FeedbackResponse>> getPendingFeedback() {
        log.info("Fetching all pending feedback");
        try {
            List<FeedbackResponse> responses = feedbackService.getPendingFeedback();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching pending feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get feedback by status
     * GET /api/feedback/status/{status}
     *
     * @param status feedback status (PENDING, APPROVED, REJECTED)
     * @return list of feedback with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByStatus(@PathVariable String status) {
        log.info("Fetching feedback with status: {}", status);
        try {
            FeedbackStatus feedbackStatus = FeedbackStatus.valueOf(status.toUpperCase());
            List<FeedbackResponse> responses = feedbackService.getFeedbackByStatus(feedbackStatus);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            log.error("Invalid feedback status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Approve feedback (authority marks as approved after correction)
     * PUT /api/feedback/{id}/approve
     *
     * @param id feedback ID to approve
     * @return updated feedback response
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<FeedbackResponse> approveFeedback(@PathVariable Long id) {
        log.info("Approving feedback: {}", id);
        try {
            FeedbackResponse response = feedbackService.approveFeedback(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Feedback not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error approving feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reject feedback (authority marks as rejected if already correct)
     * PUT /api/feedback/{id}/reject
     *
     * @param id feedback ID to reject
     * @return updated feedback response
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<FeedbackResponse> rejectFeedback(@PathVariable Long id) {
        log.info("Rejecting feedback: {}", id);
        try {
            FeedbackResponse response = feedbackService.rejectFeedback(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Feedback not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error rejecting feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
