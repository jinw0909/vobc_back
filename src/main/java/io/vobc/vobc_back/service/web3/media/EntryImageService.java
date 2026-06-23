package io.vobc.vobc_back.service.web3.media;

import io.vobc.vobc_back.domain.web3.entry.Entry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EntryImageService {
    public String uploadCoverImage(Long id, MultipartFile coverImage) {
        return null;
    }

    public String replaceContentImageAndSave(Entry entry, String content, List<MultipartFile> contentImages) {
        return null;
    }
}
