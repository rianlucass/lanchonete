package com.br.api.lanchonete.domain.stock;

public record CurrentStockDTO(
        String productId,
        String productName,
        Integer currentStock,
        Integer totalEntries,
        Integer totalExits,
        Integer totalAdjustments,
        Integer totalLosses
) {
}
