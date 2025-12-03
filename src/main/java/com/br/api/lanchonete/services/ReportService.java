package com.br.api.lanchonete.services;

import com.br.api.lanchonete.domain.product.Product;
import com.br.api.lanchonete.domain.report.*;
import com.br.api.lanchonete.repositories.OrderItemRepository;
import com.br.api.lanchonete.repositories.OrderRepository;
import com.br.api.lanchonete.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    private static final Integer LOW_STOCK_THRESHOLD = 10;

    public ReportDTO generateDailyReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // Vendas Hoje
        BigDecimal todaySales = orderRepository.getTodaySales();
        Long todayOrders = orderRepository.countTodayOrders();
        BigDecimal averageTicket = calculateAverageTicket(todaySales, todayOrders);

        // Produtos Ativos
        Long activeProducts = productRepository.countByActiveTrue();

        // Produtos Mais Vendidos Hoje (Top 5)
        List<ProductSalesDTO> topProducts = getTopSellingProductsToday(startOfDay, endOfDay);

        // Formas de Pagamento
        Map<String, BigDecimal> paymentMethods = getPaymentMethodsSummary(startOfDay, endOfDay);

        // Vendas da Semana (últimos 7 dias incluindo hoje)
        LocalDateTime startOfWeek = today.minusDays(6).atStartOfDay();
        List<DailySalesDTO> weeklySales = getWeeklySales(startOfWeek, endOfDay);

        // Estoque Baixo
        List<LowStockProductDTO> lowStockProducts = getLowStockProducts();
        Long lowStockProductsCount = (long) lowStockProducts.size();

        return new ReportDTO(
                todaySales,
                todayOrders,
                averageTicket,
                activeProducts,
                topProducts,
                paymentMethods,
                weeklySales,
                lowStockProductsCount,
                lowStockProducts
        );
    }

    private BigDecimal calculateAverageTicket(BigDecimal sales, Long orders) {
        if (orders == null || orders == 0) {
            return BigDecimal.ZERO;
        }
        return sales.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
    }

    private List<ProductSalesDTO> getTopSellingProductsToday(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = orderItemRepository.findTopSellingProductsWithNames(start, end);

        return results.stream()
                .limit(5) // Top 5 produtos mais vendidos hoje
                .map(result -> new ProductSalesDTO(
                        (String) result[0], // product name
                        ((Number) result[1]).longValue(), // quantity sold
                        (BigDecimal) result[2] // total revenue
                ))
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> getPaymentMethodsSummary(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = orderRepository.getPaymentMethodSummary(start, end);

        return results.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(), // paymentMethod
                        result -> (BigDecimal) result[1] // total amount
                ));
    }

    private List<DailySalesDTO> getWeeklySales(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = orderRepository.getDailySales(start, end);

        return results.stream()
                .map(result -> new DailySalesDTO(
                        ((java.sql.Date) result[0]).toLocalDate(),
                        (BigDecimal) result[1],
                        ((Long) result[2])
                ))
                .collect(Collectors.toList());
    }

    private List<LowStockProductDTO> getLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);

        return lowStockProducts.stream()
                .filter(Product::getActive) // Considerar apenas produtos ativos
                .map(product -> new LowStockProductDTO(
                        product.getName(),
                        product.getStock(),
                        LOW_STOCK_THRESHOLD
                ))
                .collect(Collectors.toList());
    }

    // Métodos adicionais para endpoints específicos

    public BigDecimal getTodaySales() {
        return orderRepository.getTodaySales();
    }

    public Long getTodayOrdersCount() {
        return orderRepository.countTodayOrders();
    }

    public BigDecimal getTodayAverageTicket() {
        BigDecimal sales = getTodaySales();
        Long orders = getTodayOrdersCount();
        return calculateAverageTicket(sales, orders);
    }

    public Long getActiveProductsCount() {
        return productRepository.countByActiveTrue();
    }

    public Long getLowStockProductsCount() {
        return (long) productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD)
                .stream()
                .filter(Product::getActive)
                .count();
    }

    public List<TodayTopProductDTO> getTodayTopSellingProducts(Integer limit) {
        // Se não especificar limite, retorna todos
        List<Object[]> results;

        if (limit != null && limit > 0) {
            Pageable pageable = PageRequest.of(0, limit);
            results = orderItemRepository.findTodayTopSellingProducts(pageable);
        } else {
            results = orderItemRepository.findTodayTopSellingProducts();
        }

        // Transformar os resultados com posição no ranking
        List<TodayTopProductDTO> topProducts = new ArrayList<>();
        int position = 1;

        for (Object[] result : results) {
            topProducts.add(new TodayTopProductDTO(
                    (String) result[0], // product name
                    ((Number) result[1]).longValue(), // quantity
                    (BigDecimal) result[2], // revenue
                    position
            ));
            position++;
        }

        return topProducts;
    }

    // Método para top 5 (mais comum)
    public List<TodayTopProductDTO> getTodayTop5SellingProducts() {
        return getTodayTopSellingProducts(5);
    }

    // Método para obter o produto mais vendido (top 1)
    public TodayTopProductDTO getTodayMostSoldProduct() {
        List<TodayTopProductDTO> topProducts = getTodayTopSellingProducts(1);
        if (topProducts.isEmpty()) {
            return null;
        }
        return topProducts.get(0);
    }
}