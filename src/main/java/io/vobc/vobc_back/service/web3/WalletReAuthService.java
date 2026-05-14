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
public class WalletReAuthService {

    private final WalletNonceRepository walletNonceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    public WalletReAuthResponse verify(Long userId, String walletAddress, WalletReAuthVerifyRequest request) {

        String address = normalize(walletAddress);
        String signature = request.getSignature();

        WalletNonce walletNonce = walletNonceRepository
                .findByWalletAddressAndNonce(address, request.getNonce())
                .orElseThrow(() -> new IllegalStateException("Valid Nonce not found"));

        if (walletNonce.isUsed()) {
            throw new WalletAuthException("Nonce has already been used");
        }

        if (walletNonce.isExpired(LocalDateTime.now())) {
            throw new WalletAuthException("Nonce has expired");
        }

        String recoveredAddress = recoverAddress(
                walletNonce.getMessage(),
                signature,
                address
        );

        if (!address.equalsIgnoreCase(recoveredAddress)) {
            throw new WalletAuthException("Signature verification failed");
        }

        walletNonce.markUsed();

        String reAuthToken = jwtTokenProvider.createWalletReAuthToken(
                userId,
                address
        );

        return new WalletReAuthResponse(reAuthToken);
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

            throw new WalletAuthException("Failed to recover matching address from signature");
        } catch (WalletAuthException e) {
            throw new WalletAuthException("Invalid signature format", e);
        }
    }

    private Sign.SignatureData signatureStringToData(String signature) {
        byte[] sigBytes = Numeric.hexStringToByteArray(signature);

        if (sigBytes.length != 65) {
            throw new WalletAuthException("Invalid signature length");
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
            throw new WalletAuthException("Wallet address is required");
        }

        return address.trim().toLowerCase();
    }
}
