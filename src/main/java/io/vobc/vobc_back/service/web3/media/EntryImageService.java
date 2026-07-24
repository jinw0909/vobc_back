//package io.vobc.vobc_back.service.web3.media;
//
//import io.vobc.vobc_back.domain.web3.entry.Entry;
//import io.vobc.vobc_back.exception.ImageUploadException;
//import io.vobc.vobc_back.service.S3Uploader;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EntryImageService {
//
//    private final S3Uploader s3Uploader;
//    public String uploadCoverImage(Long entryId, MultipartFile coverImage) {
//        if (coverImage == null || coverImage.isEmpty()) {
//            return null;
//        }
//
//        validateImageFile(coverImage);
//
//        try {
//
//            String assetId = "cover";
//            S3Uploader.UploadResult uploadResult = s3Uploader.uploadToS3(entryId, assetId, coverImage);
//
//            return uploadResult.url();
//
//        } catch (Exception e) {
//            log.error("Failed to upload cover image for entryId={}", entryId, e);
//            throw new ImageUploadException("Failed to upload entry cover image", e);
//        }
//    }
//
//    private void validateImageFile(MultipartFile coverImage) {
//
//    }
//
//    public String replaceContentImageAndSave(Entry entry,
//                                             String content,
//                                             List<MultipartFile> contentImages, List<String> contentImageSrcs) {
//        if (content == null || content.isBlank()) {
//            return "";
//        }
//        Map<String, MultipartFile> fileMap = buildFileMap(contentImages);
//
//        Document doc = Jsoup.parseBodyFragment(content);
//
//        for (Element img : doc.select("img")) {
//
//            String assetId = img.attr("data-asset-id").trim();
//            String src = img.attr("src").trim();
//
//
//            // data-asset-id 없으면 업로드 대상 아님
//            if (assetId.isBlank()) {
//                continue;
//            }
//
//        }
//
//        return null;
//    }
//
//    private Map<String, MultipartFile> buildFileMap(List<MultipartFile> files) {
//        Map<String, MultipartFile> map = new HashMap<>();
//
//        if (files == null || files.isEmpty()) {
//            return map;
//        }
//
//        for (MultipartFile file : files) {
//
//            if (file == null || file.isEmpty()) {
//                continue;
//            }
//
//
//        }
//
//        return map;
//
//    }
//}
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryImageService {

    private final S3Uploader s3Uploader;

    public String replaceContentImageAndSave(
            Entry entry,
            String content,
            List<MultipartFile> contentImages,
            List<String> contentImageSrcs
    ) {
        if (content == null || content.isBlank()) {
            return "";
        }

        if (contentImages == null || contentImages.isEmpty()) {
            return content;
        }

        if (contentImageSrcs == null || contentImageSrcs.isEmpty()) {
            throw new IllegalArgumentException("본문 이미지 src 정보가 없습니다.");
        }

        if (contentImages.size() != contentImageSrcs.size()) {
            throw new IllegalArgumentException("본문 이미지 파일 수와 src 수가 일치하지 않습니다.");
        }

        Document doc = Jsoup.parseBodyFragment(content);

        for (int i = 0; i < contentImages.size(); i++) {
            MultipartFile image = contentImages.get(i);
            String blobSrc = contentImageSrcs.get(i);

            if (image == null || image.isEmpty()) {
                continue;
            }

            if (blobSrc == null || blobSrc.isBlank()) {
                continue;
            }

            validateImageFile(image);

            String uploadedUrl = uploadContentImage(entry.getId(), image);

            for (Element img : doc.select("img")) {
                String src = img.attr("src").trim();

                if (src.equals(blobSrc)) {
                    img.attr("src", uploadedUrl);
                    img.removeAttr("data-asset-id");
                    break;
                }
            }
        }

        return doc.body().html();
    }

    private String uploadContentImage(Long entryId, MultipartFile image) {
        try {
//            String assetId = "content-" + index + "-" + System.currentTimeMillis();
            String assetId = "content-" + java.util.UUID.randomUUID();

            S3Uploader.UploadResult uploadResult =
                    s3Uploader.uploadEntryImageToS3(entryId, assetId, image);

            return uploadResult.url();
        } catch (Exception e) {
            log.error("Failed to upload content image for entryId={}", entryId, e);
            throw new ImageUploadException("Failed to upload entry content image", e);
        }
    }

    private void validateImageFile(MultipartFile image) {
        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB

        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 파일은 10MB 이하만 업로드할 수 있습니다.");
        }
    }
}