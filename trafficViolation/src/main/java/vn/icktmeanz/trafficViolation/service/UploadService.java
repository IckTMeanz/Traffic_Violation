package vn.icktmeanz.trafficViolation.service;

import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.entity.User;

import java.util.List;

public interface UploadService {

    UploadSessionResponse upload(UploadType uploadType, MultipartFile[] files);

    /**
     * Process uploaded files through AI model and save results to database
     * For SINGLE_IMAGE and FOLDER: processes each image
     * For VIDEO: processes first detected frame only
     */
    void processUploadedFiles(Long sessionId);

    List<UploadSessionResponse> findSessionByUser(User user);

    List<UploadSessionResponse> findSessionByStatus(SessionStatus status);

}
