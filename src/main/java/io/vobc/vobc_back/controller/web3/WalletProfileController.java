package io.vobc.vobc_back.controller.web3;

import io.vobc.vobc_back.dto.web3.PortfolioResponse;
import io.vobc.vobc_back.dto.web3.profile.ProfileImageUploadResponse;
import io.vobc.vobc_back.dto.web3.profile.SetProfileRequest;
import io.vobc.vobc_back.dto.web3.profile.WalletProfileResponse;
import io.vobc.vobc_back.dto.web3.walletuser.WalletUserResponse;
import io.vobc.vobc_back.security.jwt.JwtTokenProvider;
import io.vobc.vobc_back.security.jwt.WalletPrincipal;
import io.vobc.vobc_back.service.web3.WalletProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/web3/profile")
public class WalletProfileController {

    private final JwtTokenProvider jwtTokenProvider;
    private final WalletProfileService walletProfileService;


    @GetMapping
    public WalletProfileResponse getProfile(@AuthenticationPrincipal WalletPrincipal principal) {
        return walletProfileService.getProfile(principal.getWalletAddress());
    }


    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponse> uploadProfileImage(@AuthenticationPrincipal WalletPrincipal principal,
                                                                         @RequestPart("file") MultipartFile file) {
        if (principal == null || principal.getWalletAddress() == null) {
            return ResponseEntity.status(401).build();
        }

        ProfileImageUploadResponse response = walletProfileService.uploadProfileImage(principal.getWalletAddress(), file);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(@AuthenticationPrincipal WalletPrincipal principal,
                                          @RequestHeader("X-Reauth-Token") String reAuthToken) {

        boolean validReAuth = jwtTokenProvider.isValidWalletReAuthToken(
                reAuthToken,
                principal.getUserId(),
                principal.getWalletAddress()
        );

        if (!validReAuth) {
            return ResponseEntity.status(401).body("Re-authentication required");
        }

        PortfolioResponse response = walletProfileService.getPortfolio(
                principal.getUserId(),
                principal.getWalletAddress()
        );

        return ResponseEntity.ok(response);

    }


    @PostMapping("/update")
    public ResponseEntity<WalletUserResponse> updateProfile(@AuthenticationPrincipal WalletPrincipal principal,
                                            @RequestBody SetProfileRequest request) {
        if (principal == null || principal.getWalletAddress() == null) {
            return ResponseEntity.status(401).build();
        }
        String walletAddress = principal.getWalletAddress();
        WalletUserResponse result = walletProfileService.updateProfile(request, walletAddress);
        return ResponseEntity.ok(result);
    }


}
