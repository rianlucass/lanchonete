package com.br.api.lanchonete.domain.orders;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        String id,
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
