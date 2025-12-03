package com.br.api.lanchonete.domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesDTO(
        LocalDate date,
        BigDecimal salesAmount,
        Long ordersCount
) {
}
