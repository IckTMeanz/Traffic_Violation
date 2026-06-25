package vn.icktmeanz.trafficViolation.controller.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.dto.request.CreateAuthorityRequest;
import vn.icktmeanz.trafficViolation.dto.request.CreateUserRequest;
import vn.icktmeanz.trafficViolation.dto.response.AIRetrainStatusResponse;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.DetectedViolationRepository;
import vn.icktmeanz.trafficViolation.service.AIService;
import vn.icktmeanz.trafficViolation.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {
    private final DetectedViolationRepository detectedViolationRepository;
    private UserService userService;
    private AIService aIService;

    @Autowired
    public AdminApiController(UserService userService, DetectedViolationRepository detectedViolationRepository, AIService aIService){
        this.userService = userService;
        this.detectedViolationRepository = detectedViolationRepository;
        this.aIService = aIService;
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUsers(){
        return this.userService.getAllUsers();
    }

    @PutMapping("/changeStatus/{id}")
    public void changeStatus(@PathVariable Long id){
        this.userService.changeStatus(id);
    }

    @PostMapping("/createAuthAcc")
    public UserDTO createAuthorityAccount(@Valid @RequestBody CreateAuthorityRequest authorityRequest){
        return this.userService.createAuthorityAccount(authorityRequest);
    }

    @GetMapping("/detected-violations/count")
    public Map<String, Long> countDetectedViolations() {
        return Map.of("count", detectedViolationRepository.count());
    }

    @PostMapping("/retrain-model")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> retrainModel() {
        aIService.retrainModel();
        return Map.of("message", "Retrain model has been started");
    }

    @GetMapping("/retrain-model/status")
    public AIRetrainStatusResponse getRetrainStatus() {
        return aIService.getRetrainStatus();
    }
}

