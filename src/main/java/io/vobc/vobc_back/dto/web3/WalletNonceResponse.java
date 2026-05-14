package io.vobc.vobc_back.dto.web3;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.lang.String;

@Getter
@AllArgsConstructor
public class WalletNonceResponse {

    private String address;
    private String nonce;
    private String message;
    private LocalDateTime expiresAt;
}
