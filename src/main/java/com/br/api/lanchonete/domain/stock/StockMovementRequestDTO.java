package com.br.api.lanchonete.domain.stock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequestDTO(
        @NotBlank(message = "Product ID is required")
        String productId,

        @NotNull(message = "Movement type is required")
        MovementType movementType,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotBlank(message = "Reason is required")
        String reason,

        String originType,

        String originId
) {
}
