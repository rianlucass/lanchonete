package com.br.api.lanchonete.controllers;

import com.br.api.lanchonete.domain.report.ReportDTO;
import com.br.api.lanchonete.domain.report.TodayTopProductDTO;
import com.br.api.lanchonete.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily")
   //obter relatorio diário completo
    public ResponseEntity<ReportDTO> getDailyReport() {
        ReportDTO report = reportService.generateDailyReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/today-sales")
    //obter as vendas de hoje
    public ResponseEntity<BigDecimal> getTodaySales() {
        BigDecimal sales = reportService.getTodaySales();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/today-orders")
    //obter numero de pedidos hoje
    public ResponseEntity<Long> getTodayOrders() {
        Long orders = reportService.getTodayOrdersCount();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/today-average-ticket")
    //obter ticket medio hoje
    public ResponseEntity<BigDecimal> getTodayAverageTicket() {
        BigDecimal average = reportService.getTodayAverageTicket();
        return ResponseEntity.ok(average);
    }

    @GetMapping("/active-products")
    //obter numero de produtos ativos
    public ResponseEntity<Long> getActiveProducts() {
        Long count = reportService.getActiveProductsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/low-stock-count")
    //obter numero de produtos com estoque baixo
    public ResponseEntity<Long> getLowStockProductsCount() {
        Long count = reportService.getLowStockProductsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/today-top-products")
    //Obter produtos mais vendidos hoje
    public ResponseEntity<List<TodayTopProductDTO>> getTodayTopProducts(
            @RequestParam(required = false) Integer limit) {
        List<TodayTopProductDTO> topProducts = reportService.getTodayTopSellingProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/today-top-5-products")
    //Obter top 5 produtos mais vendidos hoje
    public ResponseEntity<List<TodayTopProductDTO>> getTodayTop5Products() {
        List<TodayTopProductDTO> topProducts = reportService.getTodayTop5SellingProducts();
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/today-most-sold-product")
    //Obter o produto mais vendido hoje
    public ResponseEntity<TodayTopProductDTO> getTodayMostSoldProduct() {
        TodayTopProductDTO mostSold = reportService.getTodayMostSoldProduct();

        if (mostSold == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(mostSold);
    }

    @GetMapping("/today-top-products/{limit}")
    //Obter top N produtos mais vendidos hoje
    public ResponseEntity<List<TodayTopProductDTO>> getTodayTopNProducts(
            @PathVariable Integer limit) {
        List<TodayTopProductDTO> topProducts = reportService.getTodayTopSellingProducts(limit);
        return ResponseEntity.ok(topProducts);
    }
}