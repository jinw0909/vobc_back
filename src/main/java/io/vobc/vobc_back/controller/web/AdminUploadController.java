package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/upload")
public class AdminUploadController {

    private final S3Uploader s3Uploader;

    @PostMapping("/thumbnail")
    @ResponseBody
    public Map<String, String> uploadThumbnail(@RequestParam("file") MultipartFile file) throws IOException {
        String url = s3Uploader.upload(file, "thumbnails");
        return Map.of("url", url);
    }

}
