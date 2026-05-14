package io.vobc.vobc_back.dto.web3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.lang.String;

@Getter
@AllArgsConstructor
public class WalletVerifyResponse {

    private String accessToken;
    private Long userId;
    private String walletAddress;

}
