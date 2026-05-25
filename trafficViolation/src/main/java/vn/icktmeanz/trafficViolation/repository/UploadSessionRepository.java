package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.icktmeanz.trafficViolation.entity.UploadSession;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
}
