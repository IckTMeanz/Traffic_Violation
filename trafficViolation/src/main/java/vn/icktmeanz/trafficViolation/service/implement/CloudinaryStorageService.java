package vn.icktmeanz.trafficViolation.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.icktmeanz.trafficViolation.constant.UploadType;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;

    public String uploadFile(
            MultipartFile file,
            UploadType uploadType,
            Long sessionId
    ) {

        try {

            String folder = switch (uploadType) {
                case SINGLE_IMAGE -> "traffic_violation/images";
                case FOLDER -> "traffic_violation/folders/session-" + sessionId;
                case VIDEO -> "traffic_violation/videos";
            };

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", UUID.randomUUID().toString(),
                            "resource_type", "auto"
                    )
            );

            return result.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Upload to Cloudinary failed", e);
        }
    }
}
