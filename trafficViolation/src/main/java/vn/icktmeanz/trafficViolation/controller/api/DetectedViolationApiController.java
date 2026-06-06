package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.icktmeanz.trafficViolation.constant.ViolationType;
import vn.icktmeanz.trafficViolation.dto.request.ChangeViolationTypeRequest;
import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.service.DetectedViolationService;

import java.util.ArrayList;
import java.util.List;

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
    public DetectedObjectDTO changeViolationTypes(@PathVariable Long ObjectId, @RequestBody ChangeViolationTypeRequest request){
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
        return this.detectedViolationService.changeViolationTypes(ObjectId, violationTypes);
    }
}
