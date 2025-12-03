package com.br.api.lanchonete.domain.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportDTO (
        // Vendas Hoje
        BigDecimal todaySales,
        Long todayOrders,
        BigDecimal averageTicket,

        // Produtos
        Long activeProducts,
        List<ProductSalesDTO> topSellingProductsToday,

        // Formas de Pagamento
        Map<String, BigDecimal> paymentMethodsSummary,

        // Vendas da Semana
        List<DailySalesDTO> weeklySales,

        // Estoque
        Long lowStockProductsCount,
        List<LowStockProductDTO> lowStockProducts
) {
}
