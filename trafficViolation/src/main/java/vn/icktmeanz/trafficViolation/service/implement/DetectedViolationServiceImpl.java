package vn.icktmeanz.trafficViolation.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.entity.DetectedViolation;
import vn.icktmeanz.trafficViolation.entity.MediaFile;
import vn.icktmeanz.trafficViolation.repository.DetectedViolationRepository;
import vn.icktmeanz.trafficViolation.service.DetectedViolationService;

import java.util.List;

@Service
public class DetectedViolationServiceImpl implements DetectedViolationService {
    private DetectedViolationRepository detectedViolationRepository;
    @Autowired
    public DetectedViolationServiceImpl(DetectedViolationRepository detectedViolationRepository){
        this.detectedViolationRepository=detectedViolationRepository;
    }

    @Override
    public List<DetectedObjectDTO> findByMedia(MediaFile mediaFile) {
        return this.detectedViolationRepository.findAllByMediaFile(mediaFile).stream()
                .map(detectedViolation -> DetectedObjectDTO.builder()
                        .objectId(Math.toIntExact(detectedViolation.getId()))
                        .violationTypes(detectedViolation.getViolationTypes())
                        .confidence(detectedViolation.getConfidence())
                        .build()).toList();
    }

    @Override
    public List<DetectedObjectDTO> findByMediaId(Long id) {
        return this.detectedViolationRepository.findAllByMediaFile_Id(id).stream()
                .map(detectedViolation -> DetectedObjectDTO.builder()
                        .objectId(Math.toIntExact(detectedViolation.getId()))
                        .violationTypes(detectedViolation.getViolationTypes())
                        .confidence(detectedViolation.getConfidence())
                        .build()).toList();
    }

    @Override
    @Transactional
    public DetectedObjectDTO changeViolationTypes(Long id, List<String> violationTypes) {
        DetectedViolation violation = this.detectedViolationRepository.findById(id).orElseThrow(()->new RuntimeException("DetectedViolation not found"));
        violation.setViolationTypes(violationTypes);
        // Set authorityCorrected to true
        violation.setAuthorityCorrected(true);
        this.detectedViolationRepository.save(violation);
        return DetectedObjectDTO.builder().
        objectId(Math.toIntExact(violation.getId()))
                .violationTypes(violationTypes).confidence(violation.getConfidence()).build();
    }
}
