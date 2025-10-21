package com.br.api.lanchonete.service;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

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
            product.setCategory(dto.category());
            product.setDescription(dto.description());
            product.setImageURL(safeFilename);

            Product savedProduct = productRepository.save(product);

            return new ProductResponseDTO(
                    savedProduct.getId(),
                    savedProduct.getName(),
                    savedProduct.getPrice()
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

}
