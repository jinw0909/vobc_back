package io.vobc.vobc_back.domain.web3;

import io.vobc.vobc_back.domain.web3.entry.EntryComment;
import io.vobc.vobc_back.domain.web3.entry.Entry;
import io.vobc.vobc_back.domain.web3.entry.EntryLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_user_id")
    private Long id;

    @Column(name = "wallet_address", nullable = false, length = 42, unique = true)
    private String walletAddress;

    @Column(name = "profile_image_url", length = 1024)
    private String profileImageUrl;

    @Column(name = "nickname", length = 32)
    private String nickname;

    @Column(name = "email")
    private String email;

    @Lob
    @Column(name = "bio")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletUserStatus status;

    @Column(name = "vob_balance", nullable = false, precision = 38, scale = 18)
    private BigDecimal vobBalance;

    @Column(name = "balance_updated_at")
    private LocalDateTime balanceUpdatedAt;

    @OneToMany(mappedBy = "walletUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entry> entryList = new ArrayList<>();

    @OneToMany(mappedBy = "walletUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntryLike> entryLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "walletUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntryComment> entryCommentList = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static WalletUser create(String walletAddress) {
        WalletUser walletUser = new WalletUser();
        walletUser.walletAddress = normalize(walletAddress);
        walletUser.status = WalletUserStatus.ACTIVE;
        walletUser.vobBalance = BigDecimal.ZERO;
        return walletUser;
    }

    public void updateBalance(BigDecimal balance, LocalDateTime balanceUpdatedAt) {
        this.vobBalance = balance;
        this.balanceUpdatedAt = balanceUpdatedAt;
    }

    private static String normalize(String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            throw new IllegalArgumentException("Wallet address cannot be null or empty");
        }
        return walletAddress.toLowerCase();
    }

}
