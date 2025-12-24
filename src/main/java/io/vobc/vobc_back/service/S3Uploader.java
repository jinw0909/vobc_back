package io.vobc.vobc_back.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    public String upload(MultipartFile file, String dirName) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = dirName + "/" + UUID.randomUUID().toString() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        // 퍼블릭 버킷이라면 이렇게 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();

    }


    public UploadResult uploadToS3(Long articleId, String assetId, MultipartFile file) throws IOException {
        String dirName = "articles/" + articleId + "/" + assetId;
        return uploadAndReturnKey(file, dirName);
    }

    public UploadResult uploadAndReturnKey(MultipartFile file, String dirName) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // 이게 곧 S3 key
        String s3Key = dirName + "/" + UUID.randomUUID() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, s3Key, file.getInputStream(), metadata);

        String url = amazonS3.getUrl(bucket, s3Key).toString();
        return new UploadResult(url, s3Key);
    }

    public void delete(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) return;
        amazonS3.deleteObject(bucket, s3Key);
    }

    public record UploadResult(String url, String s3Key) {}
}
