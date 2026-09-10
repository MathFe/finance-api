package com.mathfe.finance.controller;

import com.mathfe.finance.dto.TransactionRequestDTO;
import com.mathfe.finance.dto.TransactionResponseDTO;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO requestDTO, @AuthenticationPrincipal User user) {
        TransactionResponseDTO response = transactionService.create(requestDTO, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> listByUser(@AuthenticationPrincipal User user) {
        List<TransactionResponseDTO> response = transactionService.listByUser(user);
        return ResponseEntity.ok(response);
    }
}
