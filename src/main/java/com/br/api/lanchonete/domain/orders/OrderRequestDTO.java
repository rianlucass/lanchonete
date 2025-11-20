package com.br.api.lanchonete.domain.orders;

import java.util.List;

public record OrderRequestDTO(
        List<OrderItemRequestDTO> items,
        String clientName,
        PaymentMethod paymentMethod
) {}

