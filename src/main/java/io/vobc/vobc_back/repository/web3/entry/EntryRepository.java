package io.vobc.vobc_back.repository.web3.entry;

import io.vobc.vobc_back.domain.web3.entry.Entry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRepository extends JpaRepository<Entry, Long> {
}
