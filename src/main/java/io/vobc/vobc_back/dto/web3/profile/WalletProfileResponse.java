package io.vobc.vobc_back.dto.web3.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletProfileResponse {

    private String walletAddress;
    private String nickname;
    private String email;
    private String bio;
    private String profileImageUrl;

}
