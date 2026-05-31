package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;
import vn.icktmeanz.trafficViolation.entity.Feedback;
import vn.icktmeanz.trafficViolation.entity.UploadSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    /**
     * Find feedback by upload session
     */
    Optional<Feedback> findByUploadSession(UploadSession uploadSession);

    /**
     * Find feedback by session ID
     */
    Optional<Feedback> findByUploadSession_Id(Long sessionId);

    /**
     * Find all pending feedback (for authority to review)
     */
    List<Feedback> findByStatus(FeedbackStatus status);

    /**
     * Check if feedback exists for a session
     */
    boolean existsByUploadSession_Id(Long sessionId);
}
