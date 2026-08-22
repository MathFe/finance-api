package com.mathfe.finance.dto;

import com.mathfe.finance.entity.CategoryType;

import java.time.LocalDateTime;

public record CategoryResponseDTO(

        Long id,
        String name,
        CategoryType type,
        String color,
        LocalDateTime createdAt
) {
}
