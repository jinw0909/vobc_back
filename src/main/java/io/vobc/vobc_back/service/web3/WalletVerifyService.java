package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
import io.vobc.vobc_back.domain.web3.WalletRefreshToken;
import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.dto.web3.WalletVerifyRequest;
import io.vobc.vobc_back.dto.web3.WalletVerifyResult;
import io.vobc.vobc_back.repository.web3.WalletNonceRepository;
import io.vobc.vobc_back.repository.web3.WalletRefreshTokenRepository;
import io.vobc.vobc_back.repository.web3.WalletUserRepository;
import io.vobc.vobc_back.security.jwt.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletVerifyService {

    private final WalletNonceRepository walletNonceRepository;
    private final WalletUserRepository walletUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WalletRefreshTokenRepository walletRefreshTokenRepository;

    public WalletVerifyResult verify(WalletVerifyRequest request) {
        String address = normalize(request.getAddress());
        String signature = request.getSignature();

//        WalletNonce walletNonce = walletNonceRepository.findTopByWalletAddressAndUsedFalseOrderByCreatedAtDesc(address)
//                .orElseThrow(() -> new IllegalStateException("Valid Nonce not found"));

        WalletNonce walletNonce = walletNonceRepository.findByWalletAddressAndNonce(address, request.getNonce())
                .orElseThrow(() -> new IllegalStateException("Valid Nonce not found"));

        if (walletNonce.isUsed()) {
            throw new IllegalStateException("Nonce has already been used");
        }

        if (walletNonce.isExpired(LocalDateTime.now())) {
            throw new IllegalArgumentException("Nonce has expired");
        }

        System.out.println("request address = " + address);
        System.out.println("stored message = [" + walletNonce.getMessage() + "]");
        System.out.println("signature = " + signature);

        String recoveredAddress = recoverAddress(walletNonce.getMessage(), signature, address);
        System.out.println("recovered address = " + recoveredAddress);


        if (!address.equalsIgnoreCase(recoveredAddress)) {
            throw new IllegalArgumentException("Signature verification failed");
        }

        walletNonce.markUsed();

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
                walletUser.getNickname()
        );
    }

    private String recoverAddress(String message, String signature, String expectedAddress) {
        try {
            Sign.SignatureData signatureData = signatureStringToData(signature);

            byte[] messageHash = Sign.getEthereumMessageHash(
                    message.getBytes(StandardCharsets.UTF_8)
            );

            for (int recId = 0; recId < 4; recId++) {
                BigInteger publicKey = Sign.recoverFromSignature(
                        recId,
                        new ECDSASignature(
                                new BigInteger(1, signatureData.getR()),
                                new BigInteger(1, signatureData.getS())
                        ),
                        messageHash
                );

                if (publicKey == null) {
                    continue;
                }

                String recovered = "0x" + Keys.getAddress(publicKey);
                String normalizedRecovered = recovered.toLowerCase();

                if (normalizedRecovered.equals(expectedAddress.toLowerCase())) {
                    return normalizedRecovered;
                }
            }

            throw new IllegalArgumentException("Failed to recover matching address from signature");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid signature format", e);
        }
    }

    private Sign.SignatureData signatureStringToData(String signature) {
        byte[] sigBytes = Numeric.hexStringToByteArray(signature);

        if (sigBytes.length != 65) {
            throw new IllegalArgumentException("Invalid signature length");
        }

        byte v = sigBytes[64];
        if (v < 27) {
            v += 27;
        }

        byte[] r = Arrays.copyOfRange(sigBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(sigBytes, 32, 64);

        return new Sign.SignatureData(v, r, s);
    }

    private String normalize(@NotBlank String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Wallet address is required");
        }
        return address.trim().toLowerCase();
    }
}
