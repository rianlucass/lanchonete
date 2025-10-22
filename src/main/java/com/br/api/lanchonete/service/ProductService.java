package com.br.api.lanchonete.service;

import com.br.api.lanchonete.domain.product.Category;
import com.br.api.lanchonete.domain.product.Product;
import com.br.api.lanchonete.domain.product.ProductRequestDTO;
import com.br.api.lanchonete.domain.product.ProductResponseDTO;
import com.br.api.lanchonete.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @Transactional
    public ProductResponseDTO saveProduct(ProductRequestDTO dto) {
        if (dto.image().isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória");
        }

        String contentType = dto.image().getContentType();
        if (!Arrays.asList("image/jpeg", "image/png", "image/gif").contains(contentType)) {
            throw new IllegalArgumentException("Apenas imagens JPEG, PNG e GIF são permitidas");
        }

        if (dto.image().getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Tamanho máximo do arquivo é 5MB");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFilename = StringUtils.cleanPath(dto.image().getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String safeFilename = System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

            Path targetPath = uploadPath.resolve(safeFilename).normalize();

            if (!targetPath.startsWith(uploadPath)) {
                throw new SecurityException("Tentativa de path traversal detectada");
            }

            try (InputStream inputStream = dto.image().getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Product product = new Product();
            product.setName(dto.name());
            product.setPrice(dto.price());

            if(dto.category().equals("BEBIDAS")) {
                if(dto.stock() <= 0) {
                    throw new IllegalArgumentException("Quantidade em estoque é obrigatória para BEBIDAS");
                }
                product.setStock(dto.stock());
            }
            product.setCategory(dto.category());
            product.setDescription(dto.description());
            product.setImageURL(safeFilename);

            Product savedProduct = productRepository.save(product);

            return new ProductResponseDTO(
                    savedProduct.getName(),
                    savedProduct.getPrice(),
                    savedProduct.getCategory(),
                    savedProduct.getDescription(),
                    savedProduct.getImageURL(),
                    savedProduct.getStock()
            );

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".bin");
    }

    public List<ProductResponseDTO> getListByCategory(Category category) {
        return productRepository.findByCategory(category).stream().map(product -> {
            ProductResponseDTO responseDTO = new ProductResponseDTO(
                    product.getName(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getDescription(),
                    product.getImageURL(),
                    product.getStock()
            );

            return responseDTO;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO dto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + id));

        try {
            String imageFilename = existingProduct.getImageURL();

            if (dto.image() != null && !dto.image().isEmpty()) {
                String contentType = dto.image().getContentType();
                if (!Arrays.asList("image/jpeg", "image/png", "image/gif").contains(contentType)) {
                    throw new IllegalArgumentException("Apenas imagens JPEG, PNG e GIF são permitidas");
                }

                if (dto.image().getSize() > 5 * 1024 * 1024) {
                    throw new IllegalArgumentException("Tamanho máximo do arquivo é 5MB");
                }

                Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);

                String originalFilename = StringUtils.cleanPath(dto.image().getOriginalFilename());
                String fileExtension = getFileExtension(originalFilename);
                String safeFilename = System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

                Path targetPath = uploadPath.resolve(safeFilename).normalize();

                if (!targetPath.startsWith(uploadPath)) {
                    throw new SecurityException("Tentativa de path traversal detectada");
                }

                try (InputStream inputStream = dto.image().getInputStream()) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                try {
                    Path oldImagePath = uploadPath.resolve(existingProduct.getImageURL()).normalize();
                    if (Files.exists(oldImagePath) && oldImagePath.startsWith(uploadPath)) {
                        Files.delete(oldImagePath);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao deletar imagem antiga: " + e.getMessage());
                }

                imageFilename = safeFilename;
                existingProduct.setImageURL(imageFilename);
            }

            if (dto.name() != null && !dto.name().trim().isEmpty()) {
                existingProduct.setName(dto.name());
            }

            if (dto.price() != null && dto.price().compareTo(BigDecimal.ZERO) > 0) {
                existingProduct.setPrice(dto.price());
            }

            if (dto.category() != null && !dto.category().trim().isEmpty()) {
                try {
                    Category.valueOf(dto.category());
                    existingProduct.setCategory(dto.category());

                    if (dto.category().equals("BEBIDAS")) {
                        if (dto.stock() != null && dto.stock() <= 0) {
                            throw new IllegalArgumentException("Quantidade em estoque é obrigatória para BEBIDAS");
                        }
                        if (dto.stock() != null) {
                            existingProduct.setStock(dto.stock());
                        } else if (existingProduct.getStock() == null || existingProduct.getStock() <= 0) {
                            throw new IllegalArgumentException("Quantidade em estoque é obrigatória para BEBIDAS");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Categoria inválida: " + dto.category());
                }
            }

            if (dto.description() != null) {
                existingProduct.setDescription(dto.description());
            }

            if (dto.stock() != null && dto.stock() >= 0) {
                if (existingProduct.getCategory().equals("BEBIDAS") ||
                        (dto.category() != null && dto.category().equals("BEBIDAS"))) {
                    if (dto.stock() <= 0) {
                        throw new IllegalArgumentException("Quantidade em estoque é obrigatória para BEBIDAS");
                    }
                }
                existingProduct.setStock(dto.stock());
            }

            Product updatedProduct = productRepository.save(existingProduct);

            return new ProductResponseDTO(
                    updatedProduct.getName(),
                    updatedProduct.getPrice(),
                    updatedProduct.getCategory(),
                    updatedProduct.getDescription(),
                    updatedProduct.getImageURL(),
                    updatedProduct.getStock()
            );

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem: " + e.getMessage(), e);
        }
    }

}
