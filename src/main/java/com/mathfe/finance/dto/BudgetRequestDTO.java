package com.mathfe.finance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BudgetRequestDTO(

   @NotBlank(message = "Name is required")
   String name,

   @Min(value = 1, message = "Month must be between 1 and 12" )
   @Max(value = 12, message = "Month must be between 1 and 12")
   Integer month,

   @Min(value = 2026, message = "Year must be between 1 and 12")
   Integer year
) {
}
