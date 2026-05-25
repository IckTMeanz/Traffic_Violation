package vn.icktmeanz.trafficViolation.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.configuration.StorageProperties;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import vn.icktmeanz.trafficViolation.service.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final StorageProperties storageProperties;

    @Override
    public Path resolveStorageDirectory(UploadType uploadType, Long sessionId) {
        String basePath = switch (uploadType) {
            case SINGLE_IMAGE -> storageProperties.getImagePath();
            case FOLDER -> storageProperties.getFolderPath();
            case VIDEO -> storageProperties.getVideoPath();
        };

        Path directory = Paths.get(basePath);
        if (uploadType == UploadType.FOLDER && sessionId != null) {
            directory = directory.resolve("session-" + sessionId);
        }
        return directory;
    }

    @Override
    public String storeFile(MultipartFile file, UploadType uploadType, Long sessionId) {
        try {
            Path directory = resolveStorageDirectory(uploadType, sessionId);
            Files.createDirectories(directory);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String storedFileName = UUID.randomUUID() + extension;
            Path target = directory.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file locally: " + e.getMessage(), e);
        }
    }
}
