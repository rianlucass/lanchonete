package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
