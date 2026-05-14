package io.vobc.vobc_back.dto.web3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.lang.String;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalletNonceRequest {
    private String address;
}
