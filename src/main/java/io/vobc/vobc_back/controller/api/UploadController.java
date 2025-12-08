package io.vobc.vobc_back.controller.api;

import io.vobc.vobc_back.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final S3Uploader s3Uploader;

    @PostMapping("/api/upload/thumbnail")
    public Map<String, String> uploadThumbnail(@RequestParam("file") MultipartFile file) throws IOException {
        String url = s3Uploader.upload(file, "thumbnails");
        return Map.of("url", url);
    }
}
