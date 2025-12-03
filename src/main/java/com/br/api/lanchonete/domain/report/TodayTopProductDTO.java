package com.br.api.lanchonete.domain.report;

import java.math.BigDecimal;

public record TodayTopProductDTO(
        String productName,
        Long quantitySold,
        BigDecimal totalRevenue,
        Integer position
) {
}
