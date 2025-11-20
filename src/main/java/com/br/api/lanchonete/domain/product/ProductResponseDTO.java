package com.br.api.lanchonete.domain.product;

import java.math.BigDecimal;

public record ProductResponseDTO(
        String id,
        String name,
        BigDecimal price,
        Category category,
        String description,
        Boolean active,
        String imageURL,
        Integer stock
) {
}
