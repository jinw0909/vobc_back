package io.vobc.vobc_back.service.web3.media;

import io.vobc.vobc_back.domain.web3.entry.Entry;
import io.vobc.vobc_back.exception.ImageUploadException;
import io.vobc.vobc_back.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryImageService {

    private final S3Uploader s3Uploader;
    public String uploadCoverImage(Long entryId, MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            return null;
        }

        validateImageFile(coverImage);

        try {

            String assetId = "cover";
            S3Uploader.UploadResult uploadResult = s3Uploader.uploadToS3(entryId, assetId, coverImage);

            return uploadResult.url();

        } catch (Exception e) {
            log.error("Failed to upload cover image for entryId={}", entryId, e);
            throw new ImageUploadException("Failed to upload entry cover image", e);
        }
    }

    private void validateImageFile(MultipartFile coverImage) {

    }

    public String replaceContentImageAndSave(Entry entry,
                                             String content,
                                             List<MultipartFile> contentImages) {
        if (content == null || content.isBlank()) {
            return "";
        }
        Map<String, MultipartFile> fileMap = buildFileMap(contentImages);

        Document doc = Jsoup.parseBodyFragment(content);

        for (Element img : doc.select("img")) {

            String assetId = img.attr("data-asset-id").trim();
            String src = img.attr("src").trim();


            // data-asset-id 없으면 업로드 대상 아님
            if (assetId.isBlank()) {
                continue;
            }

        }

        return null;
    }

    private Map<String, MultipartFile> buildFileMap(List<MultipartFile> files) {
        Map<String, MultipartFile> map = new HashMap<>();

        if (files == null || files.isEmpty()) {
            return map;
        }

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }


        }

        return map;

    }
}
