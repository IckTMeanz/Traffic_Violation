package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.entity.MediaFile;

import java.util.List;

public interface DetectedViolationService {
    List<DetectedObjectDTO> findByMedia(MediaFile mediaFile);

    List<DetectedObjectDTO> findByMediaId(Long id);

    DetectedObjectDTO changeViolationTypes(Long id, List<String> violationTypes);
}
