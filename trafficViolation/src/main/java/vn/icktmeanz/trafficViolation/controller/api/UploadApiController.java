package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.dto.response.AIProcessingResultDTO;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.UploadSessionRepository;
import vn.icktmeanz.trafficViolation.repository.UserRepository;
import vn.icktmeanz.trafficViolation.service.UploadService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadApiController {

    private final UploadService uploadService;
    private final UploadSessionRepository uploadSessionRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    /**
     * Upload a single image for processing
     * @param file the image file to upload
     * @return upload session response with session ID and initial status
     */
    @PostMapping("/image")
    public ResponseEntity<UploadSessionResponse> uploadSingleImage(
            @RequestParam("file") MultipartFile file) {
        log.info("Received single image upload request");
        try {
            UploadSessionResponse response = uploadService.upload(UploadType.SINGLE_IMAGE, new MultipartFile[]{file});
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid single image upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading single image: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Upload a folder of images for batch processing
     * @param files array of image files
     * @return upload session response with session ID and initial status
     */
    @PostMapping("/folder")
    public ResponseEntity<UploadSessionResponse> uploadFolder(
            @RequestParam("files") MultipartFile[] files) {
        log.info("Received folder upload request with {} files", files.length);
        try {
            UploadSessionResponse response = uploadService.upload(UploadType.FOLDER, files);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid folder upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading folder: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Upload a video file for processing
     * @param file the video file to upload
     * @return upload session response with session ID and initial status
     */
    @PostMapping("/video")
    public ResponseEntity<UploadSessionResponse> uploadVideo(
            @RequestParam("file") MultipartFile file) {
        log.info("Received video upload request");
        try {
            UploadSessionResponse response = uploadService.upload(UploadType.VIDEO, new MultipartFile[]{file});
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid video upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Trigger AI processing for an upload session
     * @param sessionId the session ID to process
     * @return success response or error
     */
    @PostMapping("/{sessionId}/process")
    public ResponseEntity<String> processUploadSession(
            @PathVariable Long sessionId) {
        log.info("Received process request for session: {}", sessionId);
        try {
            uploadService.processUploadedFiles(sessionId);
            return ResponseEntity.ok("Processing completed successfully for session: " + sessionId);
        } catch (IllegalArgumentException e) {
            log.error("Session not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error processing session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get upload session details and detected violations
     * @param sessionId the session ID
     * @return upload session with results
     */
    @GetMapping("/{sessionId}/detail")
    public ResponseEntity<?> getSessionDetail(
            @PathVariable Long sessionId) {
        log.info("Received request for session detail: {}", sessionId);
        try {
            UploadSession session = uploadSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
            
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            log.error("Session not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching session detail: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get processing results for a media file
     * @param sessionId the session ID
     * @return AIProcessingResultDTO with violations and processed image URL
     */
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<?> getSessionResult(
            @PathVariable Long sessionId) {
        log.info("Received request for session result: {}", sessionId);
        try {
            UploadSession session = uploadSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
            
            if (session.getMediaFiles() == null || session.getMediaFiles().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            // Return first media file's violations as result
            var mediaFile = session.getMediaFiles().get(0);
            return ResponseEntity.ok(new Object() {
                public String processed_url = mediaFile.getProcessedUrl();
                public Object objects = mediaFile.getDetectedViolations();
            });
        } catch (IllegalArgumentException e) {
            log.error("Session not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching session result: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * List all upload sessions for current user
     * @return list of upload sessions
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    @GetMapping("/list")
    public ResponseEntity<List<UploadSessionResponse>> listUploadSessions() {
        log.info("Received request to list upload sessions");
        try {
            List<UploadSessionResponse> uploadSessionResponses = this.uploadService.findSessionByUser(getCurrentUser());
            return ResponseEntity.ok(uploadSessionResponses);
        } catch (Exception e) {
            log.error("Error listing upload sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete an upload session (cleanup)
     * @param sessionId the session ID to delete
     * @return success response
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<String> deleteUploadSession(
            @PathVariable Long sessionId) {
        log.info("Received delete request for session: {}", sessionId);
        try {
            uploadSessionRepository.deleteById(sessionId);
            return ResponseEntity.ok("Session " + sessionId + " deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
