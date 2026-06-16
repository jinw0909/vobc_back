package io.vobc.vobc_back.domain.web3.entry;

import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.dto.web3.entry.EntryCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_user_id", nullable = false)
    private WalletUser walletUser;

    private String title;

    private String content;

    private Integer likeCount;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL)
    private List<EntryComment> entryComments = new ArrayList<>();

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL)
    private List<EntryMedia> media = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Entry createEntry(WalletUser walletUser, EntryCreateRequest request) {
        Entry entry = new Entry();
        entry.setWalletUser(walletUser);
        walletUser.getEntryList().add(entry);
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        return entry;
    }
}
