package com.br.api.lanchonete.controllers;

import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.ProductRequestDTO;
import com.br.api.lanchonete.domain.product.ProductResponseDTO;
import com.br.api.lanchonete.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity createProduct(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam String category,
            @RequestParam int stock,
            @RequestParam(required = false) String description,
            @RequestParam MultipartFile image) {

        try {
            ProductRequestDTO dto = new ProductRequestDTO(name, price, category, stock, description, image);

            ProductResponseDTO response = productService.saveProduct(dto);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Erro interno: " + e.getMessage())
            );
        }
    }

    @GetMapping("/category/{category}")
    public List<ProductResponseDTO> getProductsByCategory(@PathVariable Category category) {
        return productService.getListByCategory(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> editarProduto(@PathVariable String id, @ModelAttribute ProductRequestDTO dto) {
        try {
            ProductResponseDTO updatedProduct = productService.updateProduct(id, dto);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
