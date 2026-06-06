package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.icktmeanz.trafficViolation.entity.MediaFile;
import vn.icktmeanz.trafficViolation.entity.UploadSession;

import java.util.List;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    List<MediaFile> findAllByUploadSession(UploadSession uploadSession);

    List<MediaFile> findAllByUploadSession_Id(Long uploadSessionId);
}
