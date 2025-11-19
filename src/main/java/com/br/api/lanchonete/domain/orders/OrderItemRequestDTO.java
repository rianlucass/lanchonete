package com.br.api.lanchonete.domain.orders;

public record OrderItemRequestDTO(
        String productId,
        int quantity
) {}

