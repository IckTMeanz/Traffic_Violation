package vn.icktmeanz.trafficViolation.service;

import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.UploadType;

import java.nio.file.Path;

public interface FileStorageService {

    Path resolveStorageDirectory(UploadType uploadType, Long sessionId);

    String storeFile(MultipartFile file, UploadType uploadType, Long sessionId);
}
