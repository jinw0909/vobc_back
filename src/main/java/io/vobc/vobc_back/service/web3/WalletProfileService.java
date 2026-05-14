package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.dto.web3.PortfolioResponse;
import io.vobc.vobc_back.dto.web3.profile.SetProfileRequest;
import io.vobc.vobc_back.dto.web3.walletuser.WalletUserResponse;
import io.vobc.vobc_back.repository.web3.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletProfileService {

    private final WalletUserRepository walletUserRepository;

    public PortfolioResponse getPortfolio(Long userId, String walletAddress) {
        return null;
    }

    @Transactional
    public WalletUserResponse updateProfile(SetProfileRequest request, String walletAddress) {

        WalletUser walletUser = walletUserRepository.findByWalletAddress(walletAddress).orElseThrow(() -> new IllegalArgumentException("Wallet address not found: " + walletAddress));
        walletUser.setNickname(request.getNickname());
        walletUser.setProfileImageUrl(request.getProfileImageUrl());
        walletUser.setEmail(request.getEmail());
        walletUser.setBio(request.getBio());
        return WalletUserResponse.from(walletUser);

    }
}
