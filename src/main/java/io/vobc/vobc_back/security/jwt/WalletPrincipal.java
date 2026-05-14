package io.vobc.vobc_back.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WalletPrincipal {
    private Long userId;
    private String walletAddress;
}
