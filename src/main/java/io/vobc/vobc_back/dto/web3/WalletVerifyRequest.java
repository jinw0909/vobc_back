package io.vobc.vobc_back.dto.web3;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.lang.String;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalletVerifyRequest {
    @NotBlank
    private String address;
    @NotBlank
    private String signature;
    @NotBlank
    private String nonce;
}
