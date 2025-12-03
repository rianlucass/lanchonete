package com.br.api.lanchonete.domain.report;

public record LowStockProductDTO(
        String productName,
        Integer currentStock,
        Integer minimumStock
) {
}
