package io.vobc.vobc_back.dto.web3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.lang.String;

@Getter
@AllArgsConstructor
public class WalletVerifyResult {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String walletAddress;
    private String profileImageUrl;
    private String nickname;
    private String email;
    private String bio;
}
