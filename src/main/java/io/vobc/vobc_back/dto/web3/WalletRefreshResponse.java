package io.vobc.vobc_back.dto.web3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.lang.String;

@Getter
@AllArgsConstructor
public class WalletRefreshResponse {

    private String walletAddress;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    private String nickname;
    private String email;
    private String bio;
    private String profileImageUrl;

}
