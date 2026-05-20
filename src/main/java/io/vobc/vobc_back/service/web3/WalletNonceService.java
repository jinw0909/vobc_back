package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
import io.vobc.vobc_back.dto.web3.WalletNonceRequest;
import io.vobc.vobc_back.dto.web3.WalletNonceResponse;
import io.vobc.vobc_back.dto.web3.WalletReAuthNonceRequest;
import io.vobc.vobc_back.repository.web3.WalletNonceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Keys;

import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletNonceService {

    private final WalletNonceRepository walletNonceRepository;
    private static final long NONCE_EXPIRE_MINUTES = 5L;
    private static final String NONCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Value("${web3.domain}")
    private String domain;

    public WalletNonceResponse issueNonce(WalletNonceRequest request) {

//        String uri = "http://localhost:3000/login";
        String uri = domain + "/login";

        String walletAddress = request.getAddress().trim();
        String checksumAddress = Keys.toChecksumAddress(walletAddress);

        String nonce = generateNonce(16); // alphanumeric only, >= 8

        ZonedDateTime issuedAt = ZonedDateTime.now(KST_ZONE).withNano(0);
        ZonedDateTime expirationTime = issuedAt.plusMinutes(NONCE_EXPIRE_MINUTES);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        String message = String.join("\n",
                removeSchemeFromDomain(domain) + " wants you to sign in with your Ethereum account:",
                checksumAddress,
                "",
                "Sign in to VOB",
                "",
                "URI: " + uri,
                "Version: 1",
                "Chain ID: 1",
                "Nonce: " + nonce,
                "Issued At: " + issuedAt.format(formatter),
                "Expiration Time: " + expirationTime.format(formatter)
        );

        WalletNonce walletNonce = WalletNonce.create(walletAddress, nonce, message, expirationTime.toLocalDateTime());
        walletNonceRepository.save(walletNonce);

        return new WalletNonceResponse(walletAddress, nonce, message, expirationTime.toLocalDateTime());
    }

    private String generateNonce(int length) {
        StringBuilder nonceBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            nonceBuilder.append(NONCE_CHARS.charAt(RANDOM.nextInt(NONCE_CHARS.length())));
        }
        return nonceBuilder.toString();
    }

    public WalletNonceResponse issueReAuthNonce(Long userId, String walletAddress, WalletReAuthNonceRequest request) {
        String action = request.getAction();

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action is required");
        }

        String checksumAddress = Keys.toChecksumAddress(walletAddress);

        String nonce = generateNonce(16);

        ZonedDateTime issuedAt = ZonedDateTime.now(KST_ZONE).withNano(0);
        ZonedDateTime expirationTime = issuedAt.plusMinutes(NONCE_EXPIRE_MINUTES);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        String uri = domain + "/profile";

        String message = String.join("\n",
                removeSchemeFromDomain(domain) + " wants you to sign in with your Ethereum account:",
                checksumAddress,
                "",
                "Re-authenticate to VOB for " + action,
                "",
                "URI: " + uri,
                "Version: 1",
                "Chain ID: 1",
                "Nonce: " + nonce,
                "Issued At: " + issuedAt.format(formatter),
                "Expiration Time: " + expirationTime.format(formatter)
        );

        WalletNonce walletNonce = WalletNonce.create(
                checksumAddress,
                nonce,
                message,
                expirationTime.toLocalDateTime()
        );

        walletNonceRepository.save(walletNonce);

        return new WalletNonceResponse(
                checksumAddress,
                nonce,
                message,
                expirationTime.toLocalDateTime()
        );
    }

    private String removeSchemeFromDomain(String domain) {
        if (domain == null) {
            return "";
        }

        return domain
                .replaceFirst("^https?://", "")
                .replaceFirst("/$", "");
    }

}
