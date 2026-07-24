package io.vobc.vobc_back.repository.web3.entry;

import io.vobc.vobc_back.domain.web3.entry.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    @EntityGraph(attributePaths = "walletUser")
    Page<Entry> findByWalletUserId(Long walletUserId, Pageable pageable);
}
