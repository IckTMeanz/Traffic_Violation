package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.icktmeanz.trafficViolation.entity.MediaFile;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}
