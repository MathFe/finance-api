package com.mathfe.finance.dto;

import com.mathfe.finance.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Type is required")
        CategoryType type,

        @NotBlank(message = "Color is required")
        String color
) {
}
