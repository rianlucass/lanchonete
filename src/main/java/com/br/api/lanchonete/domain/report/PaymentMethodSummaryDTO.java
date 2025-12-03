package com.br.api.lanchonete.domain.report;

import java.math.BigDecimal;

public record PaymentMethodSummaryDTO(
        String paymentMethod,
        BigDecimal totalAmount,
        Long ordersCount
) {
}
