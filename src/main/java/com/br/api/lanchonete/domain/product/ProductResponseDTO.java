package com.br.api.lanchonete.domain.product;

import java.math.BigDecimal;

public record ProductResponseDTO(
        String name,
        BigDecimal price,
        String category,
        String description,
        String imageURL,
        Integer stock
) {
}
