package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.icktmeanz.trafficViolation.entity.DetectedViolation;
import vn.icktmeanz.trafficViolation.entity.MediaFile;

import java.util.List;

public interface DetectedViolationRepository extends JpaRepository<DetectedViolation, Long> {
    List<DetectedViolation> findAllByMediaFile(MediaFile mediaFile);

    List<DetectedViolation> findAllByMediaFile_Id(Long mediaFileId);
}
