package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(Category categoryEnum);

    Long countByActiveTrue();
    List<Product> findByStockLessThan(Integer stock);
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock < :minimumStock")
    List<Product> findLowStockProducts(@Param("minimumStock") Integer minimumStock);

}
