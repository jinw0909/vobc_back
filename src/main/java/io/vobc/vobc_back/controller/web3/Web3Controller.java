package io.vobc.vobc_back.controller.web3;

import io.vobc.vobc_back.security.jwt.WalletPrincipal;
import io.vobc.vobc_back.service.web3.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Web3Controller {

    private final TokenService tokenService;

    @GetMapping("/web3/vob-balance")
    public ResponseEntity<?> getVobBalance(
            @AuthenticationPrincipal WalletPrincipal principal
    ) {

        if (principal == null || principal.getWalletAddress() == null) {
            throw new IllegalStateException("인증된 지갑 정보가 없습니다.");
        }

        log.info("walletAddress: {}", principal.getWalletAddress());
        String result =  tokenService.getVobBalance(principal.getWalletAddress());
//        String result =  tokenService.getBnbBalance(principal.getWalletAddress());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/web3/eth-balance")
    public ResponseEntity<?> getEthBalance() {
        return null;
    }

}
