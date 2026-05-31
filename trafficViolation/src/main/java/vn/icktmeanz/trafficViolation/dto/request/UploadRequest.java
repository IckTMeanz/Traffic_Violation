package vn.icktmeanz.trafficViolation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.icktmeanz.trafficViolation.constant.UploadType;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadRequest {
    private UploadType uploadType;
    private MultipartFile[] files;
}
