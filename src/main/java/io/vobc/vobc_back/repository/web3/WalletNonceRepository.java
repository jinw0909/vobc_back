package io.vobc.vobc_back.repository.web3;

import io.vobc.vobc_back.domain.web3.WalletNonce;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WalletNonceRepository extends JpaRepository<WalletNonce, Long> {
    Optional<WalletNonce> findTopByWalletAddressAndUsedFalseOrderByCreatedAtDesc(String walletAddress);

    Optional<WalletNonce> findByWalletAddressAndNonce(String address, @NotBlank String nonce);

//    @Query("""
//    update WalletNonce n
//       set n.status = 'INVALIDATED',
//           n.invalidatedAt = :now
//     where lower(n.walletAddress) = lower(:address)
//       and n.status = 'PENDING'
//    """)
//    int invalidatePendingNoncesByAddress(@Param("address") String address,
//                                         @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update WalletNonce n
       set n.status = 'INVALIDATED',
           n.invalidatedAt = :now
     where n.walletAddress = :address
       and n.status = 'PENDING'
    """)
    int invalidatePendingNoncesByAddress(
            @Param("address") String address,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update WalletNonce n
       set n.status = 'INVALIDATED',
           n.invalidatedAt = :now
     where n.walletAddress = :address
       and n.status = 'PENDING'
       and n.id <> :nonceId
    """)
    int invalidatePendingNoncesByAddress(
            @Param("address") String address,
            @Param("nonceId") Long usedNonceId,
            @Param("now") LocalDateTime now
    );

}
