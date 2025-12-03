package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.orders.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.product.id, SUM(oi.quantity), SUM(oi.subtotal) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.dateTime BETWEEN :start AND :end " +
            "GROUP BY oi.product.id " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsByDateRange(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("SELECT p.name, SUM(oi.quantity), SUM(oi.subtotal) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE oi.order.dateTime BETWEEN :start AND :end " +
            "AND p.active = true " +
            "GROUP BY p.id, p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsWithNames(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT p.name, SUM(oi.quantity) as totalQuantity, SUM(oi.subtotal) as totalRevenue " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE DATE(oi.order.dateTime) = CURRENT_DATE " +
            "AND oi.order.status = 'COMPLETED' " +
            "AND p.active = true " +
            "GROUP BY p.id, p.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTodayTopSellingProducts();

    // Método paginado para limitar resultados
    @Query("SELECT p.name, SUM(oi.quantity) as totalQuantity, SUM(oi.subtotal) as totalRevenue " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE DATE(oi.order.dateTime) = CURRENT_DATE " +
            "AND oi.order.status = 'COMPLETED' " +
            "AND p.active = true " +
            "GROUP BY p.id, p.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTodayTopSellingProducts(Pageable pageable);
}
