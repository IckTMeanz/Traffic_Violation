package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.response.MediaFileResponse;
import vn.icktmeanz.trafficViolation.entity.UploadSession;

import java.util.List;

public interface MediaFileService {
    List<MediaFileResponse> findBySessionId(UploadSession uploadSession);

    List<MediaFileResponse> findBySessionId(Long id);
}
