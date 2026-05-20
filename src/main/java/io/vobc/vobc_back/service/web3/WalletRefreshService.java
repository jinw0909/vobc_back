package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletRefreshToken;
import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.dto.web3.WalletRefreshResponse;
import io.vobc.vobc_back.exception.WalletAuthException;
import io.vobc.vobc_back.repository.web3.WalletRefreshTokenRepository;
import io.vobc.vobc_back.repository.web3.WalletUserRepository;
import io.vobc.vobc_back.security.jwt.JwtTokenProvider;
import io.vobc.vobc_back.security.jwt.RefreshTokenCookieProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletRefreshService {

    private final JwtTokenProvider jwtTokenProvider;
    private final WalletRefreshTokenRepository repository;
    private final RefreshTokenCookieProvider cookieProvider;
    private final WalletUserRepository walletUserRepository;

    public WalletRefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieProvider.getTokenFromCookie(request);

        if (refreshToken == null) {
            throw new WalletAuthException("Refresh token not found");
        }

        //1. JWT 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new WalletAuthException("Refresh token is invalid");
        }

        //2. wallet address 추출
        String walletAddress = jwtTokenProvider.getWalletAddress(refreshToken);

        //3. DB검증
        WalletRefreshToken saved = repository.findByWalletAddress(walletAddress)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in DB"));

        if (!saved.getRefreshToken().equals(refreshToken)) {
            throw new WalletAuthException("Refresh token mismatch");
        }

        //4. 새 access token 생성
        WalletUser walletUser = walletUserRepository.findByWalletAddress(walletAddress).orElseThrow();
        Long walletUserId = walletUser.getId();
        String newAccessToken = jwtTokenProvider.createWalletAccessToken(walletUserId, walletAddress);

        // 5. (옵션) refresh token rotation
        String newRefreshToken = jwtTokenProvider.createRefreshToken(walletAddress);
        saved.setRefreshToken(newRefreshToken);

        long refreshTokenExpirationMs = jwtTokenProvider.getRefreshTokenExpirationMs();
        saved.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));

        repository.save(saved);

        // 6. 쿠키 재세팅
        cookieProvider.addCookie(response, newRefreshToken);

        return new WalletRefreshResponse(
                walletUser.getWalletAddress(),
                newAccessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs() / 1000
        );

    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieProvider.getTokenFromCookie(request);

        if (refreshToken != null && !refreshToken.isBlank()) {
            repository.findByRefreshToken(refreshToken)
                    .ifPresent( savedToken -> {
                        savedToken.invalidate();
                        repository.save(savedToken);
                    });
        }

        cookieProvider.deleteCookie(response);
    }
}
