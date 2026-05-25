package vn.icktmeanz.trafficViolation.service;

import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;

public interface UploadService {

    UploadSessionResponse upload(UploadType uploadType, MultipartFile[] files);
}
