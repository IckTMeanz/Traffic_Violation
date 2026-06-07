package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.entity.User;

import java.util.List;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
    List<UploadSession> findAllByUser(User user);

    List<UploadSession> findAllByUserAndStatus(User user, SessionStatus status);

    List<UploadSession> findAllByStatus(SessionStatus status);
}
