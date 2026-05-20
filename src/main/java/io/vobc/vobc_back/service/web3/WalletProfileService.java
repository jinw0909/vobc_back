package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.domain.web3.WalletUser;
import io.vobc.vobc_back.dto.web3.PortfolioResponse;
import io.vobc.vobc_back.dto.web3.profile.ProfileImageUploadResponse;
import io.vobc.vobc_back.dto.web3.profile.SetProfileRequest;
import io.vobc.vobc_back.dto.web3.profile.WalletProfileResponse;
import io.vobc.vobc_back.dto.web3.walletuser.WalletUserResponse;
import io.vobc.vobc_back.repository.web3.WalletUserRepository;
import io.vobc.vobc_back.security.jwt.WalletPrincipal;
import io.vobc.vobc_back.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class WalletProfileService {

    private final WalletUserRepository walletUserRepository;
    private final S3Uploader s3Uploader;

    public PortfolioResponse getPortfolio(Long userId, String walletAddress) {
        return null;
    }

    @Transactional
    public WalletUserResponse updateProfile(SetProfileRequest request, String walletAddress) {

        WalletUser walletUser = walletUserRepository.findByWalletAddress(walletAddress).orElseThrow(() -> new IllegalArgumentException("Wallet address not found: " + walletAddress));
        walletUser.setNickname(normalizeNullable(request.getNickname()));
        walletUser.setProfileImageUrl(normalizeNullable(request.getProfileImageUrl()));
        walletUser.setEmail(normalizeNullable(request.getEmail()));
        walletUser.setBio(normalizeNullable(request.getBio()));
        return WalletUserResponse.from(walletUser);

    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    @Transactional
    public ProfileImageUploadResponse uploadProfileImage(String walletAddress, MultipartFile file) {
        validateProfileImage(file);

        try {
            String dirName = "profiles/" + walletAddress.toLowerCase();
            String imageUrl = s3Uploader.upload(file, dirName);

            return new ProfileImageUploadResponse(imageUrl);
        } catch (IOException e) {
            throw new IllegalStateException("프로필 이미지 업로드 실패", e);
        }
    }

    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 파일은 5MB 이하만 업로드할 수 있습니다.");
        }

        String contentType = file.getContentType();

        if (
                contentType == null ||
                        !(
                                contentType.equals("image/jpeg") ||
                                        contentType.equals("image/png") ||
                                        contentType.equals("image/webp") ||
                                        contentType.equals("image/gif")
                        )
        ) {
            throw new IllegalArgumentException("jpeg, png, webp, gif 이미지만 업로드할 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public WalletProfileResponse getProfile(String walletAddress) {
        WalletUser user = walletUserRepository.findByWalletAddress(walletAddress).orElseThrow(() -> new IllegalArgumentException("Wallet address not found: " + walletAddress));
        return new WalletProfileResponse(
                user.getWalletAddress(),
                user.getNickname(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl()
            );
    }
}
