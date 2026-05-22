package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
import io.vobc.vobc_back.dto.web3.WalletReAuthResponse;
import io.vobc.vobc_back.dto.web3.WalletReAuthVerifyRequest;
import io.vobc.vobc_back.exception.WalletAuthException;
import io.vobc.vobc_back.repository.web3.WalletNonceRepository;
import io.vobc.vobc_back.security.jwt.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletReAuthService {

    private final WalletNonceRepository walletNonceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WalletSignatureVerifier walletSignatureVerifier;

    public WalletReAuthResponse verify(Long userId, String walletAddress, WalletReAuthVerifyRequest request) {

        String address = normalize(walletAddress);
        String signature = request.getSignature();

        WalletNonce walletNonce = walletNonceRepository
                .findByWalletAddressAndNonce(address, request.getNonce())
                .orElseThrow(() -> new WalletAuthException("Valid Nonce not found"));

        if (walletNonce.isUsed()) {
            throw new WalletAuthException("Nonce has already been used");
        }

        if (walletNonce.isExpired(LocalDateTime.now())) {
            throw new WalletAuthException("Nonce has expired");
        }

        boolean valid = walletSignatureVerifier.verify(
                walletNonce.getMessage(),
                signature,
                address
        );

        if (!valid) {
            throw new WalletAuthException("Signature verification failed");
        }


        walletNonce.markUsed();

        String reAuthToken = jwtTokenProvider.createWalletReAuthToken(
                userId,
                address
        );

        return new WalletReAuthResponse(reAuthToken);
    }


    private String normalize(@NotBlank String address) {
        if (address == null || address.isBlank()) {
            throw new WalletAuthException("Wallet address is required");
        }

        return address.trim().toLowerCase();
    }
}
