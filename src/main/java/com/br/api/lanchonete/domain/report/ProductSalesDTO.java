package com.br.api.lanchonete.domain.report;

import java.math.BigDecimal;

public record ProductSalesDTO(
        String productName,
        Long quantitySold,
        BigDecimal totalRevenue
) {
}
