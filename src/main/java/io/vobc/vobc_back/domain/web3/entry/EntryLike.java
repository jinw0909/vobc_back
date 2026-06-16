package io.vobc.vobc_back.domain.web3.entry;

import io.vobc.vobc_back.domain.web3.WalletUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(
    name = "entry_like",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"entry_id", "wallet_user_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntryLike {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_user_id", nullable = false)
    private WalletUser walletUser;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
