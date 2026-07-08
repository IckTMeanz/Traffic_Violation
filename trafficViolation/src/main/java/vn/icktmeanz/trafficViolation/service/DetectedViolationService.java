package vn.icktmeanz.trafficViolation.service;

import java.util.List;

import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.entity.MediaFile;

public interface DetectedViolationService {
    List<DetectedObjectDTO> findByMedia(MediaFile mediaFile);

    List<DetectedObjectDTO> findByMediaId(Long id);

    DetectedObjectDTO changeViolationTypes(Long id, List<String> violationTypes);

    DetectedObjectDTO changeViolationTypes(String cropUuid, List<String> violationTypes);
}
