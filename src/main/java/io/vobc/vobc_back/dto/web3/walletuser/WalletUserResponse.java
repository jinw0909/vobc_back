package io.vobc.vobc_back.dto.web3.walletuser;

import io.vobc.vobc_back.domain.web3.WalletUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WalletUserResponse {
    private Long id;
    private String walletAddress;
    private String profileImageUrl;
    private String nickname;
    private String email;
    private String bio;
    private BigDecimal vobBalance;
    private LocalDateTime balanceUpdatedAt;

    public static WalletUserResponse from(WalletUser walletUser) {
        return new WalletUserResponse(
                walletUser.getId(),
                walletUser.getWalletAddress(),
                walletUser.getProfileImageUrl(),
                walletUser.getNickname(),
                walletUser.getEmail(),
                walletUser.getBio(),
                walletUser.getVobBalance(),
                walletUser.getBalanceUpdatedAt()
        );
    }

}
