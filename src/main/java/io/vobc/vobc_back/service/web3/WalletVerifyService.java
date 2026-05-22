package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletVerifyService {

    private final WalletNonceRepository walletNonceRepository;
    private final WalletUserRepository walletUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WalletRefreshTokenRepository walletRefreshTokenRepository;

    private static final String BASE_RPC_URL = "https://mainnet.base.org";
    private static final String ERC1271_MAGIC_VALUE = "0x1626ba7e";

    public WalletVerifyResult verify(WalletVerifyRequest request) {

        log.info("WalletVerifyService.verify()");
        String address = normalize(request.getAddress());
        String signature = request.getSignature();

//        WalletNonce walletNonce = walletNonceRepository.findTopByWalletAddressAndUsedFalseOrderByCreatedAtDesc(address)
//                .orElseThrow(() -> new IllegalStateException("Valid Nonce not found"));

        WalletNonce walletNonce = walletNonceRepository.findByWalletAddressAndNonce(address, request.getNonce())
                .orElseThrow(() -> new WalletAuthException("Valid Nonce not found"));

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

        boolean valid = verifySignature(walletNonce.getMessage(), signature, address);

        if (!valid) {
            throw new WalletAuthException("Signature verification failed");
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
                walletUser.getNickname(),
                walletUser.getEmail(),
                walletUser.getBio()
        );
    }

    private boolean verifySmartWalletSignature(String message, String signature, String walletAddress) {
        try {
            Web3j web3j = Web3j.build(new HttpService(BASE_RPC_URL));

            byte[] messageHash = Sign.getEthereumMessageHash(
                    message.getBytes(StandardCharsets.UTF_8)
            );

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

            Function function = new Function(
                    "isValidSignature",
                    List.of(
                            new Bytes32(messageHash),
                            new DynamicBytes(signatureBytes)
                    ),
                    List.of(new TypeReference<Bytes4>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);

            Transaction transaction = Transaction.createEthCallTransaction(
                    null,
                    walletAddress,
                    encodedFunction
            );

            String result = web3j.ethCall(
                    transaction,
                    DefaultBlockParameterName.LATEST
            ).send().getValue();

            log.info("ERC1271 isValidSignature result={}", result);

            if (result == null || result.equals("0x")) {
                return false;
            }

            List<org.web3j.abi.datatypes.Type> decoded =
                    FunctionReturnDecoder.decode(result, function.getOutputParameters());

            if (decoded.isEmpty()) {
                return false;
            }

            Bytes4 magicValue = (Bytes4) decoded.get(0);
            String returnedMagicValue = Numeric.toHexString(magicValue.getValue());

            log.info("ERC1271 returnedMagicValue={}", returnedMagicValue);

            return ERC1271_MAGIC_VALUE.equalsIgnoreCase(returnedMagicValue);
        } catch (Exception e) {
            log.warn("Smart wallet signature verification failed. wallet={}, error={}",
                    walletAddress,
                    e.getMessage()
            );
            return false;
        }
    }


    private boolean verifySignature(String message, String signature, String expectedAddress) {
        if (!isValidHexSignature(signature)) {
            throw new WalletAuthException("Invalid signature format");
        }

        byte[] sigBytes = Numeric.hexStringToByteArray(signature);

        if (sigBytes.length == 65) {
            String recoveredAddress = recoverAddress(message, signature, expectedAddress);
            log.debug("recovered address = {}", recoveredAddress);
            return expectedAddress.equalsIgnoreCase(recoveredAddress);
        }

        log.info("Smart wallet signature detected. signatureBytesLength={}", sigBytes.length);

        return verifySmartWalletSignature(message, signature, expectedAddress);
    }

    private boolean isValidHexSignature(String signature) {
        if (signature == null || !signature.startsWith("0x")) {
            return false;
        }

        String hex = signature.substring(2);

        return !hex.isBlank()
                && hex.length() % 2 == 0
                && hex.matches("^[0-9a-fA-F]+$");
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
            throw new IllegalArgumentException("Wallet address is required");
        }
        return address.trim().toLowerCase();
    }
}
