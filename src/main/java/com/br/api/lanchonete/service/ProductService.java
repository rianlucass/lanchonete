package com.br.api.lanchonete.service;

import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.Product;
import com.br.api.lanchonete.domain.product.ProductRequestDTO;
import com.br.api.lanchonete.domain.product.ProductResponseDTO;
import com.br.api.lanchonete.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @Transactional
    public ProductResponseDTO saveProduct(ProductRequestDTO dto) {
        validateImage(dto.image());
        validateStockForBebidas(dto.category(), dto.stock());

        try {
            String filename = saveImage(dto.image());

            Product product = new Product();
            product.setName(dto.name());
            product.setPrice(dto.price());
            product.setCategory(dto.category());
            product.setDescription(dto.description());
            product.setActive(true);
            product.setImageURL(filename);
            product.setStock(dto.stock());


            Product saved = productRepository.save(product);

            return mapToResponse(saved);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        try {
            if (dto.image() != null && !dto.image().isEmpty()) {
                validateImage(dto.image());
                deleteImage(product.getImageURL());
                String newFilename = saveImage(dto.image());
                product.setImageURL(newFilename);
            }

            if (dto.name() != null && !dto.name().isBlank()) {
                product.setName(dto.name());
            }

            if (dto.price() != null && dto.price().compareTo(BigDecimal.ZERO) > 0) {
                product.setPrice(dto.price());
            }

            if (dto.category() != null) {
                validateStockForBebidas(dto.category(), dto.stock());
                product.setCategory(dto.category());
            }

            if (dto.description() != null) {
                product.setDescription(dto.description());
            }

            if (dto.stock() != null) {
                validateStockForBebidas(
                        dto.category() != null ? dto.category() : product.getCategory(),
                        dto.stock()
                );
                product.setStock(dto.stock());
            }

            Product updated = productRepository.save(product);
            return mapToResponse(updated);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem: " + e.getMessage(), e);
        }
    }

    public List<ProductResponseDTO> getListByCategory(String category) {
        try {
            Category categoryEnum = Category.valueOf(category.toUpperCase());
            return productRepository.findByCategoryAndActiveTrue(categoryEnum).stream().map(product -> {
                ProductResponseDTO responseDTO = new ProductResponseDTO(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getCategory(),
                        product.getDescription(),
                        product.getActive(),
                        product.getImageURL(),
                        product.getStock()
                );
                return responseDTO;
            }).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida -> " + category);
        }
    }

    public List<ProductResponseDTO> findAllProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.getActive()) // ← Filtra apenas ativos
                .map(product -> {
                    ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getDescription(),
                            product.getActive(),
                            product.getImageURL(),
                            product.getStock()
                    );
                    return productResponseDTO;
                }).toList();
    }


    public String softDelete(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        product.setActive(false);
        productRepository.save(product);

        return "Produto: " + product.getName() + ", ID: " + product.getId() + "Desativado";
    }


    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");

        if (!allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("Apenas imagens JPEG, PNG e GIF são permitidas");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Tamanho máximo do arquivo é 5MB");
        }
    }

    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);

        String safeFilename = System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;

        Path targetPath = uploadPath.resolve(safeFilename).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new SecurityException("Tentativa de path traversal detectada");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return safeFilename;
    }

    private void deleteImage(String filename) {
        if (filename == null) return;

        try {
            Path path = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            System.err.println("Erro ao deletar imagem antiga: " + e.getMessage());
        }
    }

    private void validateStockForBebidas(Category category, Integer stock) {
        if (category == Category.BEBIDAS) {
            if (stock == null || stock <= 0) {
                throw new IllegalArgumentException("Quantidade em estoque é obrigatória para BEBIDAS");
            }
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".bin");
    }

    private ProductResponseDTO mapToResponse(Product p) {
        return new ProductResponseDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCategory(),
                p.getDescription(),
                p.getActive(),
                p.getImageURL(),
                p.getStock()
        );
    }
}
