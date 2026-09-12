package com.mathfe.finance.controller;

import com.mathfe.finance.dto.CryptoDashboardResponseDTO;
import com.mathfe.finance.dto.CryptoHoldingRequestDTO;
import com.mathfe.finance.dto.CryptoHoldingResponseDTO;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.service.CryptoHoldingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoHoldingService cryptoHoldingService;

    public CryptoController(CryptoHoldingService cryptoHoldingService) {
        this.cryptoHoldingService = cryptoHoldingService;
    }

    @GetMapping
    public ResponseEntity<CryptoDashboardResponseDTO> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cryptoHoldingService.getDashboard(user));
    }

    @PostMapping
    public ResponseEntity<CryptoHoldingResponseDTO> addHolding(@Valid @RequestBody CryptoHoldingRequestDTO requestDTO, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cryptoHoldingService.addHolding(requestDTO, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeHolding(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            cryptoHoldingService.removeHolding(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
