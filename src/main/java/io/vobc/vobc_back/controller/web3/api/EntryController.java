package io.vobc.vobc_back.controller.web3.api;

import io.vobc.vobc_back.domain.web3.entry.Entry;
import io.vobc.vobc_back.dto.web3.entry.EntryCreateRequest;
import io.vobc.vobc_back.dto.web3.entry.EntryCreateResponse;
import io.vobc.vobc_back.dto.web3.entry.EntryResponse;
import io.vobc.vobc_back.security.CustomUserDetails;
import io.vobc.vobc_back.security.jwt.WalletPrincipal;
import io.vobc.vobc_back.service.web3.entry.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web3/api/entry")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @PostMapping("/create")
    public ResponseEntity<EntryCreateResponse> create(@RequestBody EntryCreateRequest request,
                                                      @AuthenticationPrincipal WalletPrincipal walletPrincipal) {

        if (walletPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Entry entry = entryService.create(walletPrincipal.getUserId(), request);
        EntryCreateResponse response = EntryCreateResponse.from(entry);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<EntryResponse>> list(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EntryResponse> result = entryService.list(pageable);
        return ResponseEntity.ok(result);
    }
}
