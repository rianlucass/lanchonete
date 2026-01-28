package com.br.api.lanchonete.services;

import com.br.api.lanchonete.domain.orders.*;
import com.br.api.lanchonete.domain.product.Product;
import com.br.api.lanchonete.domain.stock.MovementType;
import com.br.api.lanchonete.domain.stock.StockMovementRequestDTO;
import com.br.api.lanchonete.exceptions.InsufficientStockException;
import com.br.api.lanchonete.exceptions.ProductNotFoundException;
import com.br.api.lanchonete.repositories.OrderRepository;
import com.br.api.lanchonete.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockService stockService;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Order order = new Order();
        order.setDateTime(LocalDateTime.now());
        order.setClientName(orderRequestDTO.clientName());
        order.setPaymentMethod(orderRequestDTO.paymentMethod());
        order.setStatus(OrderStatus.COMPLETED);
        order.setOrderNumber(generateOrderNumber());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for(OrderItemRequestDTO itemDTO : orderRequestDTO.items()){
            Product product = productRepository.findById(itemDTO.productId()).orElseThrow(()-> new ProductNotFoundException(itemDTO.productId()));

            // Registra saída de estoque através do StockService
            stockService.registerMovement(new StockMovementRequestDTO(
                product.getId(),
                MovementType.EXIT,
                itemDTO.quantity(),
                "Venda - Pedido #" + order.getOrderNumber() + " - Cliente: " + orderRequestDTO.clientName(),
                "ORDER",
                null  // Será atualizado com order.getId() após salvar
            ));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDTO.quantity());
            item.setUnitPrice(product.getPrice());

            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.quantity())));
            total = total.add(item.getSubtotal());
            items.add(item);
        }

        order.setTotalAmount(total);
        order.setItems(items);
        orderRepository.save(order);

        return mapToResponse(order);
    }

    private OrderResponseDTO mapToResponse(Order order) {
        List<OrderItemResponseDTO> itemResponses = order.getItems().stream().map(item ->
                new OrderItemResponseDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                )
        ).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getDateTime(),
                order.getClientName(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                itemResponses
        );
    }

    public List<OrderResponseDTO> getHistory(LocalDate startDate, LocalDate endDate, OrderStatus status) {

        List<Order> orders;

        if (startDate != null && endDate != null && status != null) {
            orders = orderRepository.findByStatusAndDateTimeBetween(
                    status,
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59)
            );
        }
        else if (startDate != null && endDate != null) {
            orders = orderRepository.findByDateTimeBetween(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59)
            );
        }
        else if (status != null) {
            orders = orderRepository.findByStatus(status);
        }
        else {
            orders = orderRepository.findAll();
        }

        return orders.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new RuntimeException("Esse pedido já foi cancelado.");
        }

        // Reverter estoque
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        return mapToResponse(order);
    }

    private String generateOrderNumber() {
        long count = orderRepository.count() + 1;
        return String.format("PED-%06d", count);
    }


}
