package com.br.api.lanchonete.controllers;

import com.br.api.lanchonete.domain.orders.OrderRequestDTO;
import com.br.api.lanchonete.domain.orders.OrderResponseDTO;
import com.br.api.lanchonete.domain.orders.OrderStatus;
import com.br.api.lanchonete.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO dto) {
        OrderResponseDTO response = orderService.createOrder(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public List<OrderResponseDTO> history(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) OrderStatus status
    ) {
        return orderService.getHistory(startDate, endDate, status);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponseDTO cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

}
