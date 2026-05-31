package vn.icktmeanz.trafficViolation.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.AIProcessingResultDTO;
import vn.icktmeanz.trafficViolation.dto.response.BoundingBoxDTO;
import vn.icktmeanz.trafficViolation.dto.response.DetectedObjectDTO;
import vn.icktmeanz.trafficViolation.dto.response.MediaFileResponse;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.entity.DetectedViolation;
import vn.icktmeanz.trafficViolation.entity.MediaFile;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.DetectedViolationRepository;
import vn.icktmeanz.trafficViolation.repository.MediaFileRepository;
import vn.icktmeanz.trafficViolation.repository.UploadSessionRepository;
import vn.icktmeanz.trafficViolation.repository.UserRepository;
import vn.icktmeanz.trafficViolation.service.AIService;
import vn.icktmeanz.trafficViolation.service.FileStorageService;
import vn.icktmeanz.trafficViolation.service.UploadService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private final FileStorageService fileStorageService;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

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
            String storedPath = fileStorageService.storeFile(file, uploadType, session.getId());
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
                case FOLDER -> processFolder(mediaFiles); // Đã sửa đổi sang gửi mảng tệp tin
                case VIDEO -> processVideo(mediaFiles.getFirst());
            }

            session.setStatus(SessionStatus.AI_PROCESSED);
            uploadSessionRepository.save(session);
            log.info("Successfully finished async processing for session: {}", sessionId);

        } catch (Exception e) {
            log.error("Error processing async session: {}", sessionId, e);
            // Có thể bổ sung cập nhật trạng thái lỗi FAILED tại đây nếu bảng trạng thái cho phép
            throw new RuntimeException("Failed to process uploaded files: " + e.getMessage(), e);
        }
    }

    private void processSingleImage(MediaFile mediaFile) {
        log.info("Processing single image file: {}", mediaFile.getId());
        processMediaFile(mediaFile);
    }

    /**
     * SỬA LỖI: Gom toàn bộ danh sách tệp tin của Folder gửi sang Flask nhận mảng JSON kết quả
     */
    private void processFolder(List<MediaFile> mediaFiles) {
        log.info("Processing batch folder with {} images concurrently via Flask", mediaFiles.size());
        try {
            // Chuyển đổi danh sách đường dẫn chuỗi thành mảng các đối tượng File tương ứng
            File[] filesToSend = mediaFiles.stream()
                    .map(mf -> new File(mf.getOriginalUrl()))
                    .toArray(File[]::new);

            // Gọi API xử lý Folder của Flask nhận về danh sách kết quả List<AIProcessingResultDTO>
            // Lưu ý: Đảm bảo interface AIService đã được khai báo trả về List<AIProcessingResultDTO> tại phương thức processFolder
            List<AIProcessingResultDTO> results = aiService.processFolder(filesToSend);

            // Ánh xạ tuần tự kết quả trả về tương ứng với danh sách mediaFiles trong DB
            for (int i = 0; i < mediaFiles.size(); i++) {
                MediaFile mediaFile = mediaFiles.get(i);
                
                // Phòng ngừa trường hợp danh sách Flask trả về lệch kích thước do lỗi xử lý file cụ thể
                if (i >= results.size()) break; 

                AIProcessingResultDTO fileResult = results.get(i);

                // Cập nhật đường dẫn ảnh kết quả đã được vẽ bounding box
                mediaFile.setProcessedUrl(fileResult.getProcessedUrl());
                mediaFileRepository.save(mediaFile);

                // Lưu các chi tiết vi phạm của bức ảnh hiện tại
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

    private void processVideo(MediaFile mediaFile) {
        log.info("Processing video file: {}", mediaFile.getId());
        try {
            File videoFile = new File(mediaFile.getOriginalUrl());
            AIProcessingResultDTO aiResult = aiService.processVideo(videoFile);

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
            String imagePath = mediaFile.getOriginalUrl();
            AIProcessingResultDTO aiResult = aiService.processImage(imagePath);

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
}