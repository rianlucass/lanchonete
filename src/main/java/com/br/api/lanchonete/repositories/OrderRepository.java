package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
