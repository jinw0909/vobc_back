package io.vobc.vobc_back.dto.web3.entry;

import lombok.Data;

@Data
public class EntryCreateRequest {

    private String title;
    private String content;
    private String coverImageUrl;

}
