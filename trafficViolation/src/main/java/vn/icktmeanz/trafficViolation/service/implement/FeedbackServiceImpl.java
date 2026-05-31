package vn.icktmeanz.trafficViolation.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;
import vn.icktmeanz.trafficViolation.dto.request.CreateFeedbackRequest;
import vn.icktmeanz.trafficViolation.dto.response.FeedbackResponse;
import vn.icktmeanz.trafficViolation.entity.Feedback;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.FeedbackRepository;
import vn.icktmeanz.trafficViolation.repository.UploadSessionRepository;
import vn.icktmeanz.trafficViolation.repository.UserRepository;
import vn.icktmeanz.trafficViolation.service.FeedbackService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final UserRepository userRepository;

    @Override
    public FeedbackResponse createFeedback(CreateFeedbackRequest request) {
        log.info("Creating feedback for session: {}", request.getUploadSessionId());

        // Get current user from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get upload session
        UploadSession session = uploadSessionRepository.findById(request.getUploadSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        // Check if feedback already exists (1:1 constraint)
        if (feedbackRepository.existsByUploadSession_Id(request.getUploadSessionId())) {
            throw new IllegalArgumentException("Feedback already exists for this session");
        }

        // Create feedback
        Feedback feedback = Feedback.builder()
                .uploadSession(session)
                .user(currentUser)
                .description(request.getFeedbackContent())
                .status(FeedbackStatus.PENDING)
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback created successfully with ID: {}", savedFeedback.getId());

        return mapToResponse(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return mapToResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackBySessionId(Long sessionId) {
        Feedback feedback = feedbackRepository.findByUploadSession_Id(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found for session: " + sessionId));
        return mapToResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getPendingFeedback() {
        log.info("Fetching all pending feedback");
        return feedbackRepository.findByStatus(FeedbackStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByStatus(FeedbackStatus status) {
        log.info("Fetching feedback with status: {}", status);
        return feedbackRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackResponse approveFeedback(Long feedbackId) {
        log.info("Approving feedback: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        // Get current authority user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authority = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authority not found"));

        feedback.setStatus(FeedbackStatus.APPROVED);
        feedback.setHandledBy(authority);
        feedback.setHandledAt(LocalDateTime.now());

        Feedback updated = feedbackRepository.save(feedback);
        log.info("Feedback approved by: {}", authority.getUsername());

        return mapToResponse(updated);
    }

    @Override
    public FeedbackResponse rejectFeedback(Long feedbackId) {
        log.info("Rejecting feedback: {}", feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        // Get current authority user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authority = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authority not found"));

        feedback.setStatus(FeedbackStatus.REJECTED);
        feedback.setHandledBy(authority);
        feedback.setHandledAt(LocalDateTime.now());

        Feedback updated = feedbackRepository.save(feedback);
        log.info("Feedback rejected by: {}", authority.getUsername());

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean feedbackExists(Long sessionId) {
        return feedbackRepository.existsByUploadSession_Id(sessionId);
    }

    /**
     * Convert Feedback entity to FeedbackResponse DTO
     */
    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .uploadSessionId(feedback.getUploadSession().getId())
                .userFullName(feedback.getUser().getFullName())
                .userPhone(feedback.getUser().getPhoneNumber())
                .description(feedback.getDescription())
                .status(feedback.getStatus())
                .handledByName(feedback.getHandledBy() != null ? feedback.getHandledBy().getFullName() : null)
                .handledAt(feedback.getHandledAt())
                .createdAt(feedback.getCreatedAt() != null ? feedback.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
