package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
