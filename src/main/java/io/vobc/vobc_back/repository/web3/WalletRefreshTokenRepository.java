package io.vobc.vobc_back.repository.web3;

import io.vobc.vobc_back.domain.web3.WalletRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRefreshTokenRepository extends JpaRepository<WalletRefreshToken, Long> {

    Optional<WalletRefreshToken> findByWalletAddress(String walletAddress);

    Optional<WalletRefreshToken> findByRefreshToken(String refreshToken);

    void deleteByWalletAddress(String walletAddress);
}
