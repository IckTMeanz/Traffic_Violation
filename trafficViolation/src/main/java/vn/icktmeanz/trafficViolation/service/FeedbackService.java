package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;
import vn.icktmeanz.trafficViolation.dto.request.CreateFeedbackRequest;
import vn.icktmeanz.trafficViolation.dto.response.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    /**
     * Create feedback/appeal for an upload session
     */
    FeedbackResponse createFeedback(CreateFeedbackRequest request);

    /**
     * Get feedback by ID
     */
    FeedbackResponse getFeedbackById(Long id);

    /**
     * Get feedback by upload session ID
     */
    FeedbackResponse getFeedbackBySessionId(Long sessionId);

    /**
     * Get all pending feedback (for authority review)
     */
    List<FeedbackResponse> getPendingFeedback();

    /**
     * Get all feedback with specific status
     */
    List<FeedbackResponse> getFeedbackByStatus(FeedbackStatus status);

    /**
     * Approve feedback (authority marks as approved after correction)
     */
    FeedbackResponse approveFeedback(Long feedbackId);

    /**
     * Reject feedback (authority marks as rejected if already correct)
     */
    FeedbackResponse rejectFeedback(Long feedbackId);

    /**
     * Check if feedback exists for a session
     */
    boolean feedbackExists(Long sessionId);
}
