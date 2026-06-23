package io.vobc.vobc_back.service.web3.entry;

import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.domain.web3.entry.Entry;
import io.vobc.vobc_back.dto.web3.entry.EntryCreateRequest;
import io.vobc.vobc_back.dto.web3.entry.EntryResponse;
import io.vobc.vobc_back.repository.web3.WalletUserRepository;
import io.vobc.vobc_back.repository.web3.entry.EntryRepository;
import io.vobc.vobc_back.service.web3.media.EntryImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final WalletUserRepository WalletUserRepository;
    private final EntryImageService entryImageService;



//    @Transactional
//    public Entry create(Long userId, EntryCreateRequest request) {
//        WalletUser walletUser = WalletUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Wallet user not found: " + userId));
//        Entry entry = Entry.createEntry(walletUser, request);
//        entryRepository.save(entry);
//        return entry;
//    }

    @Transactional(readOnly = true)
    public Page<EntryResponse> list(Pageable pageable) {
        return null;
    }

    public Entry create(Long userId,
                        EntryCreateRequest request,
                        MultipartFile coverImage,
                        List<MultipartFile> contentImages) {
        WalletUser user = WalletUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Entry entry = Entry.createEntry(
                user,
                request
        );

        entryRepository.save(entry);

        String coverImageUrl = null;

        if (coverImage != null && !coverImage.isEmpty()) {
            coverImageUrl = entryImageService.uploadCoverImage(entry.getId(), coverImage);
            entry.changeCoverImageUrl(coverImageUrl);
        }

        String finalContent = entryImageService.replaceContentImageAndSave(
                entry,
                request.getContent(),
                contentImages
        );

        entry.changeContent(finalContent);

        return entry;
    }
}
