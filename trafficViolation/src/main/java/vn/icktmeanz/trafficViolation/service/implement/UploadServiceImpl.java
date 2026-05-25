package vn.icktmeanz.trafficViolation.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.SessionStatus;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.dto.response.MediaFileResponse;
import vn.icktmeanz.trafficViolation.dto.response.UploadSessionResponse;
import vn.icktmeanz.trafficViolation.entity.MediaFile;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.MediaFileRepository;
import vn.icktmeanz.trafficViolation.repository.UploadSessionRepository;
import vn.icktmeanz.trafficViolation.repository.UserRepository;
import vn.icktmeanz.trafficViolation.service.FileStorageService;
import vn.icktmeanz.trafficViolation.service.UploadService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv");

    private final UploadSessionRepository uploadSessionRepository;
    private final MediaFileRepository mediaFileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

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
}
