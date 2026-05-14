package io.vobc.vobc_back.repository.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletNonceRepository extends JpaRepository<WalletNonce, Long> {
    Optional<WalletNonce> findTopByWalletAddressAndUsedFalseOrderByCreatedAtDesc(String walletAddress);

    Optional<WalletNonce> findByWalletAddressAndNonce(String address, @NotBlank String nonce);
}
