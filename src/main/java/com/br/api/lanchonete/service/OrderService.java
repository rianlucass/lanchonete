package com.br.api.lanchonete.service;

import com.br.api.lanchonete.domain.orders.*;
import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.Product;
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

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Order order = new Order();
        order.setDateTime(LocalDateTime.now());
        order.setClientName(orderRequestDTO.clientName());
        order.setPaymentMethod(orderRequestDTO.paymentMethod());
        order.setStatus(OrderStatus.COMPLETED);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for(OrderItemRequestDTO itemDTO : orderRequestDTO.items()){
            Product product = productRepository.findById(itemDTO.productId()).orElseThrow(()-> new ProductNotFoundException(itemDTO.productId()));

            if (product.getStock() < itemDTO.quantity()){
                throw new InsufficientStockException(product.getName());
            }
            product.setStock(product.getStock() - itemDTO.quantity());


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
                order.getDateTime(),
                order.getClientName(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                itemResponses
        );
    }

    public List<OrderResponseDTO> getHistory(LocalDate startDate, LocalDate endDate) {
        List<Order> orders;

        if (startDate != null && endDate != null) {
            orders = orderRepository.findByDateTimeBetween(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59)
            );
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream().map(this::mapToResponse).toList();
    }

}
