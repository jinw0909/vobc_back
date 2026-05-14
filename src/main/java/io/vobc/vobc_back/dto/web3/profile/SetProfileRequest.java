package io.vobc.vobc_back.dto.web3.profile;

import lombok.Data;

@Data
public class SetProfileRequest {
    private String nickname;
    private String profileImageUrl;
    private String email;
    private String bio;
    private String memberStatus;
}
