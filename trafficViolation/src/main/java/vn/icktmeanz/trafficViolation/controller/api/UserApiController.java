package vn.icktmeanz.trafficViolation.controller.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.dto.request.CreateUserRequest;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.service.UserService;
import vn.icktmeanz.trafficViolation.service.UploadService;

@RestController
@RequestMapping("/user/api")
public class UserApiController {

    private final UserService userService;
    private final UploadService uploadService;

    @Autowired
    public UserApiController(UserService userService, UploadService uploadService) {
        this.userService = userService;
        this.uploadService = uploadService;
    }

    @PostMapping("/createUserAcc")
    public UserDTO createUserAccount(@Valid @RequestBody CreateUserRequest userRequest) {
        return this.userService.createUser(userRequest);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadSessionResponse> upload(
            @RequestParam("uploadType") UploadType uploadType,
            @RequestParam("files") MultipartFile[] files) {
        UploadSessionResponse response = uploadService.upload(uploadType, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/process/{sessionId}")
    public ResponseEntity<String> processUploadedFiles(@PathVariable Long sessionId) {
        uploadService.processUploadedFiles(sessionId);
        return ResponseEntity.ok("Files processed successfully");
    }
}
