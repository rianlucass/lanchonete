package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.orders.Order;
import com.br.api.lanchonete.domain.orders.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByStatusAndDateTimeBetween(OrderStatus status, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    List<Order> findByStatus(OrderStatus status);

    // Consultas para relatório
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE DATE(o.dateTime) = CURRENT_DATE AND o.status = 'COMPLETED'")
    BigDecimal getTodaySales();

    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.dateTime) = CURRENT_DATE AND o.status = 'COMPLETED'")
    Long countTodayOrders();

    @Query("SELECT o.paymentMethod, SUM(o.totalAmount) FROM Order o " +
            "WHERE DATE(o.dateTime) = CURRENT_DATE AND o.status = 'COMPLETED' " +
            "GROUP BY o.paymentMethod")
    List<Object[]> getPaymentMethodSummary(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT DATE(o.dateTime), SUM(o.totalAmount), COUNT(o) " +
            "FROM Order o " +
            "WHERE o.dateTime >= :start AND o.status = 'COMPLETED' " +
            "GROUP BY DATE(o.dateTime) " +
            "ORDER BY DATE(o.dateTime) DESC")
    List<Object[]> getDailySales(@Param("start") LocalDateTime start, LocalDateTime end);

}
