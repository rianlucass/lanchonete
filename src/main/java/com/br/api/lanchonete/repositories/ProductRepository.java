package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(Category categoryEnum);

}
