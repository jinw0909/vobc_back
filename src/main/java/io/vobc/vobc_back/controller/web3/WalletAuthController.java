package io.vobc.vobc_back.controller.web3;

import io.vobc.vobc_back.dto.web3.*;
import io.vobc.vobc_back.security.jwt.RefreshTokenCookieProvider;
import io.vobc.vobc_back.security.jwt.WalletPrincipal;
import io.vobc.vobc_back.service.web3.WalletNonceService;
import io.vobc.vobc_back.service.web3.WalletReAuthService;
import io.vobc.vobc_back.service.web3.WalletRefreshService;
import io.vobc.vobc_back.service.web3.WalletVerifyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/web3/auth")
public class WalletAuthController {

    private final WalletNonceService walletNonceService;
    private final WalletVerifyService walletVerifyService;
    private final WalletRefreshService walletRefreshService;
    private final RefreshTokenCookieProvider cookieProvider;
    private final WalletReAuthService walletReAuthService;

    // nonce (간단하게 param)
    @PostMapping("/nonce")
    public WalletNonceResponse issueNonce(@RequestBody WalletNonceRequest request) {
        return walletNonceService.issueNonce(request);
    }

    // verify (DTO 사용)
    @PostMapping("/verify")
    public WalletVerifyResponse verify(@RequestBody WalletVerifyRequest request,
                                       HttpServletResponse response) {

        WalletVerifyResult result = walletVerifyService.verify(request);


        cookieProvider.addCookie(response, result.getRefreshToken());

        return new WalletVerifyResponse(
                result.getAccessToken(),
                result.getUserId(),
                result.getWalletAddress(),
                result.getNickname(),
                result.getEmail(),
                result.getBio(),
                result.getProfileImageUrl()
        );
    }

    @PostMapping("/reauth/nonce")
    public WalletNonceResponse issueReAuthNonce(@AuthenticationPrincipal WalletPrincipal walletPrincipal,
                                                @RequestBody WalletReAuthNonceRequest request) {
        return walletNonceService.issueReAuthNonce(walletPrincipal.getUserId(), walletPrincipal.getWalletAddress(), request);
    }

    @PostMapping("/reauth/verify")
    public WalletReAuthResponse reAuthVerify(@AuthenticationPrincipal WalletPrincipal walletPrincipal,
                                             @RequestBody WalletReAuthVerifyRequest request) {
        return walletReAuthService.verify(walletPrincipal.getUserId(), walletPrincipal.getWalletAddress(), request);
    }


    @PostMapping("/refresh")
    public WalletRefreshResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return walletRefreshService.refresh(request, response);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        walletRefreshService.logout(request, response);
    }

}
