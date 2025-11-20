package com.br.api.lanchonete.domain.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        String id,
        LocalDateTime dateTime,
        String clientName,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        OrderStatus status,
        List<OrderItemResponseDTO> items
) {
}
