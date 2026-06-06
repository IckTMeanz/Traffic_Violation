package vn.icktmeanz.trafficViolation.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.icktmeanz.trafficViolation.dto.response.MediaFileResponse;
import vn.icktmeanz.trafficViolation.service.MediaFileService;
import vn.icktmeanz.trafficViolation.service.UploadService;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaApiController {
    private MediaFileService mediaFileService;

    @Autowired
    public MediaApiController(MediaFileService mediaFileService){
        this.mediaFileService=mediaFileService;
    }

    @GetMapping("/{SessionId}/list")
    public List<MediaFileResponse> mediaFileResponseList(@PathVariable Long SessionId){
        return this.mediaFileService.findBySessionId(SessionId);
    }
}
