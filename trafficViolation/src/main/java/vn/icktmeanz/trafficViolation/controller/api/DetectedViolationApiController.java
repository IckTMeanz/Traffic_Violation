package vn.icktmeanz.trafficViolation.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.icktmeanz.trafficViolation.constant.ViolationType;
import vn.icktmeanz.trafficViolation.dto.request.ChangeViolationTypeRequest;
import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.service.DetectedViolationService;

@RestController
@RequestMapping("/api/violation")
@RequiredArgsConstructor
@Slf4j
public class DetectedViolationApiController {
    private DetectedViolationService detectedViolationService;
    @Autowired
    public DetectedViolationApiController(DetectedViolationService detectedViolationService){
        this.detectedViolationService=detectedViolationService;
    }

    @GetMapping("/{mediaId}/list")
    public List<DetectedObjectDTO> getDetectedObjectsByMediaId(@PathVariable Long mediaId){
        return this.detectedViolationService.findByMediaId(mediaId);
    }

    @PostMapping("/change/{ObjectId}")
    public DetectedObjectDTO changeViolationTypes(@PathVariable("ObjectId") String objectId, @RequestBody ChangeViolationTypeRequest request){
        ArrayList<String> violationTypes = new ArrayList<>();
        if (request.getNo_helmet()==1){
            violationTypes.add(ViolationType.NO_HELMET.name());
        }
        if (request.getUsing_phone()==1){
            violationTypes.add(ViolationType.USING_PHONE.name());
        }
        if (request.getTriple_riding()==1){
            violationTypes.add(ViolationType.TRIPLE_RIDING.name());
        }
        return this.detectedViolationService.changeViolationTypes(objectId, violationTypes);
    }
}
