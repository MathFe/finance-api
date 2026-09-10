package com.mathfe.finance.controller;

import com.mathfe.finance.dto.BudgetRequestDTO;
import com.mathfe.finance.dto.BudgetResponseDTO;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BudgetRequestDTO requestDTO, @AuthenticationPrincipal User user) {
        try {
            BudgetResponseDTO response = budgetService.create(requestDTO, user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> listByUser(@AuthenticationPrincipal User user) {
        List<BudgetResponseDTO> response = budgetService.listByUser(user);
        return ResponseEntity.ok(response);
    }
}
