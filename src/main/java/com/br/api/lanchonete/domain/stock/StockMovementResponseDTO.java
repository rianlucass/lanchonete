package com.br.api.lanchonete.domain.stock;

import java.time.LocalDateTime;

public record StockMovementResponseDTO(
        String id,
        String productId,
        String productName,
        MovementType movementType,
        Integer quantity,
        LocalDateTime dateTime,
        String responsibleUser,
        String reason,
        String originType,
        String originId
) {
}
