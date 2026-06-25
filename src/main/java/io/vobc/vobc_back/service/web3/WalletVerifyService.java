package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
import io.vobc.vobc_back.domain.web3.WalletNonceStatus;
import io.vobc.vobc_back.domain.web3.WalletRefreshToken;
import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.dto.web3.WalletVerifyRequest;
import io.vobc.vobc_back.dto.web3.WalletVerifyResult;
import io.vobc.vobc_back.exception.WalletAuthException;
import io.vobc.vobc_back.repository.web3.WalletNonceRepository;
import io.vobc.vobc_back.repository.web3.WalletRefreshTokenRepository;
import io.vobc.vobc_back.repository.web3.WalletUserRepository;
import io.vobc.vobc_back.security.jwt.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletVerifyService {

    private final WalletNonceRepository walletNonceRepository;
    private final WalletUserRepository walletUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WalletRefreshTokenRepository walletRefreshTokenRepository;
    private final WalletSignatureVerifier walletSignatureVerifier;

//    private static final String BASE_RPC_URL = "https://mainnet.base.org";

    @Value("${web3.base.rpc-url}")
    private String baseRpcUrl;

    private static final String ERC1271_MAGIC_VALUE = "0x1626ba7e";

    public WalletVerifyResult verify(WalletVerifyRequest request) {

        log.info("WalletVerifyService.verify()");
        String address = normalize(request.getAddress());
        String signature = request.getSignature();

//        WalletNonce walletNonce = walletNonceRepository.findTopByWalletAddressAndUsedFalseOrderByCreatedAtDesc(address)
//                .orElseThrow(() -> new IllegalStateException("Valid Nonce not found"));

        WalletNonce walletNonce = walletNonceRepository.findByWalletAddressAndNonce(address, request.getNonce())
                .orElseThrow(() -> new WalletAuthException("Valid Nonce not found"));

        if (walletNonce.getStatus() != WalletNonceStatus.PENDING) {
            throw new WalletAuthException("Nonce is not pending");
        }

        if (walletNonce.isUsed()) {
            throw new WalletAuthException("Nonce has already been used");
        }

        if (walletNonce.isExpired(LocalDateTime.now())) {
            throw new WalletAuthException("Nonce has expired");
        }

        log.debug("request address = {}", address);

//        String recoveredAddress = recoverAddress(walletNonce.getMessage(), signature, address);
//
//        log.debug("recovered address = {}", recoveredAddress);
//
//        if (!address.equalsIgnoreCase(recoveredAddress)) {
//            throw new WalletAuthException("Signature verification failed");
//        }

//        boolean valid = verifySignature(walletNonce.getMessage(), signature, address);
//
//        if (!valid) {
//            throw new WalletAuthException("Signature verification failed");
//        }

        boolean valid = walletSignatureVerifier.verify(
                walletNonce.getMessage(),
                signature,
                address
        );

        if (!valid) {
            throw new WalletAuthException("Signature verification failed");
        }

        walletNonce.markUsed();

        walletNonceRepository.invalidatePendingNoncesByAddress(address, walletNonce.getId(), LocalDateTime.now());

        WalletUser walletUser = walletUserRepository.findByWalletAddress(address)
                .orElseGet(() -> walletUserRepository.save(WalletUser.create(address)));

        String accessToken = jwtTokenProvider.createWalletAccessToken(
                walletUser.getId(),
                walletUser.getWalletAddress()
        );


        String refreshToken = jwtTokenProvider.createRefreshToken(walletUser.getWalletAddress());

        WalletRefreshToken saved = walletRefreshTokenRepository.findByWalletAddress(walletUser.getWalletAddress())
                .orElseGet(() -> WalletRefreshToken.create(walletUser));


        saved.updateToken(refreshToken, LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000));

        walletRefreshTokenRepository.save(saved);

        return new WalletVerifyResult(
                accessToken,
                refreshToken,
                walletUser.getId(),
                walletUser.getWalletAddress(),
                walletUser.getProfileImageUrl(),
                walletUser.getNickname(),
                walletUser.getEmail(),
                walletUser.getBio()
        );
    }


    private String normalize(@NotBlank String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Wallet address is required");
        }
        return address.trim().toLowerCase();
    }
}
