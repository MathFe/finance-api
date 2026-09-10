package com.mathfe.finance.controller;

import com.mathfe.finance.dto.CategoryRequestDTO;
import com.mathfe.finance.dto.CategoryResponseDTO;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO requestDTO, @AuthenticationPrincipal User user) {
        CategoryResponseDTO response = categoryService.create(requestDTO, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listByUser(@AuthenticationPrincipal User user) {
        List<CategoryResponseDTO> response = categoryService.listByUser(user);
        return ResponseEntity.ok(response);
    }
}
