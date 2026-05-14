package io.vobc.vobc_back.domain.web3;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletNonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_nonce_id")
    private Long id;

    @Column(name = "wallet_address", nullable = false, length = 42)
    private String walletAddress;

    @Column(nullable = false, unique = true, length = 120)
    private String nonce;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static WalletNonce create(String walletAddress, String nonce, String message, LocalDateTime expiresAt) {
        WalletNonce walletNonce = new WalletNonce();
        walletNonce.walletAddress = normalize(walletAddress);
        walletNonce.nonce = nonce;
        walletNonce.message = message;
        walletNonce.expiresAt = expiresAt;
        return walletNonce;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    private static String normalize(String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            throw new IllegalArgumentException("Wallet address cannot be null or empty");
        }
        return walletAddress.toLowerCase();
    }
}
