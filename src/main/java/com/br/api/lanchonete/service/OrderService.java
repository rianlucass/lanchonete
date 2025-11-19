package com.br.api.lanchonete.service;

import com.br.api.lanchonete.domain.orders.*;
import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.Product;
import com.br.api.lanchonete.repositories.OrderRepository;
import com.br.api.lanchonete.repositories.ProductRepository;
import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.core.io.BigIntegerParser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Formatter;
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
        order.setPaymentMethod(orderRequestDTO.paymentMethod());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.valueOf(0);

        for(OrderItemRequestDTO itemDTO : orderRequestDTO.items()){
            Product product = productRepository.findById(itemDTO.productId()).orElseThrow(()-> new RuntimeException("Product not Found: " + itemDTO.productId() ));

            if (product.getCategory() == Category.BEBIDAS){
                if (product.getStock() < itemDTO.quantity()){
                    throw new RuntimeException("Insufficiente stock for product: " + product.getName());
                }
                product.setStock(product.getStock() - itemDTO.quantity());
                productRepository.save(product);
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDTO.quantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.quantity())));
            total.add(item.getSubtotal());
        }
        return null;
    }

}
