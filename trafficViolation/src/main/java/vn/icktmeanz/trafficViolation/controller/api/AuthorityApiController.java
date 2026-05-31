package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.service.UploadService;

@RestController
@RequestMapping("/authority/api")
@RequiredArgsConstructor
public class AuthorityApiController {

    private final UploadService uploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadSessionResponse> upload(
            @RequestParam("uploadType") UploadType uploadType,
            @RequestParam("files") MultipartFile[] files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadService.upload(uploadType, files));
    }

    @PostMapping("/process/{sessionId}")
    public ResponseEntity<String> processUploadedFiles(@PathVariable Long sessionId) {
        uploadService.processUploadedFiles(sessionId);
        return ResponseEntity.ok("Files processed successfully");
    }
}
