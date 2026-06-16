package io.vobc.vobc_back.dto.web3.entry;

import io.vobc.vobc_back.domain.web3.entry.Entry;

public record EntryCreateResponse(Long id) {

    public static EntryCreateResponse from(Entry entry) {
        return new EntryCreateResponse(entry.getId());
    }
}
