package io.vobc.vobc_back.repository.web3;

import io.vobc.vobc_back.domain.web3.WalletUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletUserRepository extends JpaRepository<WalletUser, Long> {

    Optional<WalletUser> findByWalletAddress(String walletAddress);
}
