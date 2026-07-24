package io.vobc.vobc_back.dto.web3.entry;

import io.vobc.vobc_back.domain.web3.entry.Entry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EntryResponse {

    private Long id;
    private String title;
    private String content;
    private String coverImageUrl;

    private Integer likeCount;
//    private Integer commentCount;

    private Long walletUserId;
    private String walletAddress;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EntryResponse from(Entry entry) {
        return new EntryResponse(
                entry.getId(),
                entry.getTitle(),
                entry.getContent(),
                entry.getCoverImageUrl(),
                entry.getLikeCount(),
                entry.getWalletUser().getId(),
                entry.getWalletUser().getWalletAddress(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

}
