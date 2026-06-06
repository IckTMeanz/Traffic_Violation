package vn.icktmeanz.trafficViolation.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.icktmeanz.trafficViolation.dto.response.MediaFileResponse;
import vn.icktmeanz.trafficViolation.entity.UploadSession;
import vn.icktmeanz.trafficViolation.repository.MediaFileRepository;
import vn.icktmeanz.trafficViolation.service.MediaFileService;

import java.util.List;

@Service
public class MediaFileServiceImpl implements MediaFileService {
    private MediaFileRepository mediaFileRepository;

    @Autowired
    public MediaFileServiceImpl(MediaFileRepository mediaFileRepository){
        this.mediaFileRepository=mediaFileRepository;
    }

    @Override
    public List<MediaFileResponse> findBySessionId(UploadSession uploadSession) {
        return this.mediaFileRepository.findAllByUploadSession(uploadSession)
                .stream().map(mediaFile -> MediaFileResponse.builder()
                        .id(mediaFile.getId())
                        .aiStatus(mediaFile.getAiStatus())
                        .originalUrl(mediaFile.getOriginalUrl())
                        .processedUrl(mediaFile.getProcessedUrl())
                        .build()).toList();
    }

    @Override
    public List<MediaFileResponse> findBySessionId(Long id) {
        return this.mediaFileRepository.findAllByUploadSession_Id(id)
                .stream().map(mediaFile -> MediaFileResponse.builder()
                        .id(mediaFile.getId())
                        .aiStatus(mediaFile.getAiStatus())
                        .originalUrl(mediaFile.getOriginalUrl())
                        .processedUrl(mediaFile.getProcessedUrl())
                        .build()).toList();
    }
}
