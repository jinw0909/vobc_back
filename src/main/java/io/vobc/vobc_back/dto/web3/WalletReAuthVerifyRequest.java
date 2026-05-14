package io.vobc.vobc_back.dto.web3;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.lang.String;
@Getter
@NoArgsConstructor
public class WalletReAuthVerifyRequest {
    private String message;
    private String signature;
    private String action;
    private String nonce;
}
