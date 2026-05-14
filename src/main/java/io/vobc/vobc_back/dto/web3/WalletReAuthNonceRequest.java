package io.vobc.vobc_back.dto.web3;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.lang.String;

@Getter
@NoArgsConstructor
public class WalletReAuthNonceRequest {
    private String action;
}
