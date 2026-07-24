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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/web3/api/entry")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntryCreateResponse> create(
            @RequestPart("request") EntryCreateRequest request,
            @RequestPart(value = "contentImages", required = false) List<MultipartFile> contentImages,
            @RequestParam(value = "contentImageSrcs", required = false) List<String> contentImageSrcs,
            @AuthenticationPrincipal WalletPrincipal walletPrincipal
    ) {
        if (walletPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Entry entry = entryService.create(
                walletPrincipal.getUserId(),
                request,
                contentImages == null ? List.of() : contentImages,
                contentImageSrcs == null ? List.of() : contentImageSrcs
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EntryCreateResponse.from(entry));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<EntryResponse>> list(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EntryResponse> result = entryService.list(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<EntryResponse>> myEntries(@AuthenticationPrincipal WalletPrincipal walletPrincipal,
                                                         @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        if (walletPrincipal != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<EntryResponse> result = entryService.myEntries(walletPrincipal.getUserId(), pageable);
        return ResponseEntity.ok(result);
    }

}
