package io.vobc.vobc_back.domain.web3;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @Column(nullable = false, length = 42)
    private String walletAddress;

    @Column(nullable = false, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static WalletRefreshToken create(WalletUser walletUser) {
        return WalletRefreshToken.builder()
                .walletAddress(walletUser.getWalletAddress())
                .refreshToken("")
                .expiresAt(LocalDateTime.now())
                .build();
    }

    public void updateToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public void invalidate() {
        this.refreshToken = "INVALIDATED_" + System.currentTimeMillis();;
        this.expiresAt = LocalDateTime.now().minusSeconds(1);
    }
}
