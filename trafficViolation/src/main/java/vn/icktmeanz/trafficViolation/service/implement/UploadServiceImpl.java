package vn.icktmeanz.trafficViolation.service.implement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.icktmeanz.trafficViolation.constant.FeedbackStatus;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.AIProcessingResultDTO;
import vn.icktmeanz.trafficViolation.dto.response.BoundingBoxDTO;
import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.dto.response.MediaFileResponse;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.entity.*;
import vn.icktmeanz.trafficViolation.repository.*;
import vn.icktmeanz.trafficViolation.service.AIService;
import vn.icktmeanz.trafficViolation.service.FileStorageService;
import vn.icktmeanz.trafficViolation.service.UploadService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv");

    private final UploadSessionRepository uploadSessionRepository;
    private final MediaFileRepository mediaFileRepository;
    private final DetectedViolationRepository detectedViolationRepository;
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final AIService aiService;
    private final ObjectMapper objectMapper;
    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public UploadSessionResponse upload(UploadType uploadType, MultipartFile[] files) {
        validateFiles(uploadType, files);

        User user = getCurrentUser();

        UploadSession session = UploadSession.builder()
                .user(user)
                .uploadType(uploadType)
                .status(SessionStatus.PROCESSING)
                .build();

        session = uploadSessionRepository.save(session);

        List<MediaFile> mediaFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            String storedPath = cloudinaryStorageService.uploadFile(file, uploadType, session.getId());
            MediaFile mediaFile = MediaFile.builder()
                    .uploadSession(session)
                    .originalUrl(storedPath)
                    .aiStatus("UNCHECKED")
                    .build();
            mediaFiles.add(mediaFileRepository.save(mediaFile));
        }

        if (uploadType == UploadType.VIDEO && !mediaFiles.isEmpty()) {
            session.setVideoUrl(mediaFiles.getFirst().getOriginalUrl());
            session = uploadSessionRepository.save(session);
        }

        return toResponse(session, mediaFiles);
    }

    private void validateFiles(UploadType uploadType, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one file is required.");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Empty files are not allowed.");
            }
        }

        switch (uploadType) {
            case SINGLE_IMAGE -> {
                if (files.length != 1) {
                    throw new IllegalArgumentException("Single image upload requires exactly one file.");
                }
                assertImage(files[0]);
            }
            case VIDEO -> {
                if (files.length != 1) {
                    throw new IllegalArgumentException("Video upload requires exactly one file.");
                }
                assertVideo(files[0]);
            }
            case FOLDER -> {
                if (files.length < 1) {
                    throw new IllegalArgumentException("Folder upload requires at least one image.");
                }
                for (MultipartFile file : files) {
                    assertImage(file);
                }
            }
        }
    }

    private void assertImage(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid image file: " + file.getOriginalFilename() + ". Allowed: jpg, jpeg, png, webp.");
        }
    }

    private void assertVideo(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        if (!VIDEO_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid video file: " + file.getOriginalFilename() + ". Allowed: mp4, mov, avi, mkv.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    private UploadSessionResponse toResponse(UploadSession session, List<MediaFile> mediaFiles) {
        List<MediaFileResponse> mediaResponses = mediaFiles.stream()
                .map(mf -> MediaFileResponse.builder()
                        .id(mf.getId())
                        .originalUrl(mf.getOriginalUrl())
                        .aiStatus(mf.getAiStatus())
                        .build())
                .toList();

        return UploadSessionResponse.builder()
                .sessionId(session.getId())
                .uploadType(session.getUploadType())
                .status(session.getStatus())
                .videoUrl(session.getVideoUrl())
                .createdAt(session.getCreatedAt())
                .mediaFiles(mediaResponses)
                .build();
    }

    /**
     * TỐI ƯU: Đánh dấu @Async xử lý bất đồng bộ, giải phóng luồng chính cho Controller
     */
    @Override
    @Async("taskExecutor")
    @Transactional
    public void processUploadedFiles(Long sessionId) {
        log.info("Asynchronously processing uploaded files for session: {}", sessionId);

        UploadSession session = uploadSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Upload session not found: " + sessionId));

        List<MediaFile> mediaFiles = session.getMediaFiles();
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            log.warn("No media files found for session: {}", sessionId);
            session.setStatus(SessionStatus.AI_PROCESSED);
            uploadSessionRepository.save(session);
            return;
        }

        try {
            switch (session.getUploadType()) {
                case SINGLE_IMAGE -> processSingleImage(mediaFiles.getFirst());
                case FOLDER -> processFolder(mediaFiles);
                case VIDEO -> processVideo(mediaFiles.getFirst());
            }

            session.setStatus(SessionStatus.AI_PROCESSED);
            uploadSessionRepository.save(session);
            log.info("Successfully finished async processing for session: {}", sessionId);

        } catch (Exception e) {
            log.error("Error processing async session: {}", sessionId, e);
            throw new RuntimeException("Failed to process uploaded files: " + e.getMessage(), e);
        }
    }

    private void processSingleImage(MediaFile mediaFile) {
        log.info("Processing single image via URL: {}", mediaFile.getId());
        processMediaFile(mediaFile);
    }

    /**
     * ĐÃ SỬA LỖI: Lấy danh sách chuỗi URL từ Cloudinary truyền thẳng sang AIService
     */
    private void processFolder(List<MediaFile> mediaFiles) {
        log.info("Processing batch folder with {} images via URLs", mediaFiles.size());
        try {
            // Gom tất cả các URL Cloudinary dạng chuỗi vào một List
            List<String> urlsToSend = mediaFiles.stream()
                    .map(MediaFile::getOriginalUrl)
                    .toList();

            // Gửi sang Flask API xử lý theo danh sách URL
            List<AIProcessingResultDTO> results = aiService.processFolder(urlsToSend);

            for (int i = 0; i < mediaFiles.size(); i++) {
                MediaFile mediaFile = mediaFiles.get(i);
                if (i >= results.size()) break;

                AIProcessingResultDTO fileResult = results.get(i);

                mediaFile.setProcessedUrl(fileResult.getProcessedUrl());
                mediaFileRepository.save(mediaFile);

                if (fileResult.getObjects() != null && !fileResult.getObjects().isEmpty()) {
                    for (DetectedObjectDTO detectedObject : fileResult.getObjects()) {
                        saveDetectedViolation(mediaFile, detectedObject);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error encountered during batch folder processing", e);
            throw new RuntimeException("Failed batch processing folder: " + e.getMessage(), e);
        }
    }

    /**
     * ĐÃ SỬA LỖI: Truyền URL trực tiếp thay vì khởi tạo đối tượng File cục bộ
     */
    private void processVideo(MediaFile mediaFile) {
        log.info("Processing video via URL: {}", mediaFile.getId());
        try {
            String videoUrl = mediaFile.getOriginalUrl();
            AIProcessingResultDTO aiResult = aiService.processVideo(videoUrl);

            mediaFile.setProcessedUrl(aiResult.getProcessedUrl());
            mediaFile = mediaFileRepository.save(mediaFile);

            if (aiResult.getObjects() != null && !aiResult.getObjects().isEmpty()) {
                for (DetectedObjectDTO detectedObject : aiResult.getObjects()) {
                    saveDetectedViolation(mediaFile, detectedObject);
                }
            }
            log.info("Successfully processed video file: {}", mediaFile.getId());
        } catch (RuntimeException e) {
            log.error("Error processing video file: {}", mediaFile.getId(), e);
            throw new RuntimeException("Failed to process video file " + mediaFile.getId() + ": " + e.getMessage(), e);
        }
    }

    private void processMediaFile(MediaFile mediaFile) {
        try {
            String imageUrl = mediaFile.getOriginalUrl();
            AIProcessingResultDTO aiResult = aiService.processImage(imageUrl);

            mediaFile.setProcessedUrl(aiResult.getProcessedUrl());
            mediaFile = mediaFileRepository.save(mediaFile);

            if (aiResult.getObjects() != null && !aiResult.getObjects().isEmpty()) {
                for (DetectedObjectDTO detectedObject : aiResult.getObjects()) {
                    saveDetectedViolation(mediaFile, detectedObject);
                }
            }
        } catch (RuntimeException e) {
            log.error("Error processing media file: {}", mediaFile.getId(), e);
            throw new RuntimeException("Failed to process media file " + mediaFile.getId() + ": " + e.getMessage(), e);
        }
    }

    private void saveDetectedViolation(MediaFile mediaFile, DetectedObjectDTO detectedObject) {
        try {
            BoundingBoxDTO bboxDTO = detectedObject.getBoundingBox();
            String boundingBoxJson;
            try {
                boundingBoxJson = objectMapper.writeValueAsString(bboxDTO);
            } catch (JsonProcessingException e) {
                log.error("Error serializing bounding box for media file: {}", mediaFile.getId(), e);
                throw new RuntimeException("Failed to serialize bounding box: " + e.getMessage(), e);
            }

            DetectedViolation violation = DetectedViolation.builder()
                    .mediaFile(mediaFile)
                    .violationTypes(detectedObject.getViolationTypes())
                    .boundingBox(boundingBoxJson)
                    .confidence(detectedObject.getConfidence())
                    .frameNumber(detectedObject.getFrameNumber())
                    .isAuthorityCorrected(false)
                    .build();

            detectedViolationRepository.save(violation);
            log.debug("Saved detected violation for media file: {}, object_id: {}", 
                    mediaFile.getId(), detectedObject.getObjectId());

        } catch (RuntimeException e) {
            log.error("Error saving detected violation for media file: {}", mediaFile.getId(), e);
            throw new RuntimeException("Failed to save detected violation: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UploadSessionResponse> findSessionByUser(User user) {
        return uploadSessionRepository.findAllByUser(user)
                .stream()
                .map(session -> UploadSessionResponse.builder()
                        .sessionId(session.getId())
                        .uploadType(session.getUploadType())
                        .status(session.getStatus())
                        .createdAt(session.getCreatedAt())
                        .build())
                .toList();
    }

    //For normal user
    @Override
    public List<UploadSessionResponse> findSessionByStatus(SessionStatus status) {
        return uploadSessionRepository
            .findAllByUserAndStatus(getCurrentUser(), status)
                .stream()
                .map(session -> UploadSessionResponse.builder()
                        .sessionId(session.getId())
                        .uploadType(session.getUploadType())
                        .status(session.getStatus())
                        .createdAt(session.getCreatedAt())
                        .build())
                .toList();
    }

    //For authority
    @Override
    public List<UploadSessionResponse> findSessionByStatusFB(SessionStatus status) {
        return uploadSessionRepository
                .findAllByStatus(status)
                .stream()
                .map(session -> UploadSessionResponse.builder()
                        .sessionId(session.getId())
                        .uploadType(session.getUploadType())
                        .user_id(session.getUser().getId())
                        .createdAt(session.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void updateSessionStatus(Long sessionId, SessionStatus status) {
        UploadSession uploadSession = this.uploadSessionRepository.findById(sessionId).orElseThrow(()-> new RuntimeException("Cannot find upload session with id: " + sessionId));
        uploadSession.setStatus(status);
        Feedback feedback = this.feedbackRepository.findByUploadSession_Id(sessionId).orElseThrow(()-> new RuntimeException("Cannot find feedback with ss id: " + sessionId));
        feedback.setHandledBy(getCurrentUser());
        feedback.setHandledAt(java.time.LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.APPROVED);
        this.feedbackRepository.save(feedback);
        this.uploadSessionRepository.save(uploadSession);
    }
}