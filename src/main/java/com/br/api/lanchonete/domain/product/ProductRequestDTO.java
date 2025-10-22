package com.br.api.lanchonete.domain.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ProductRequestDTO (
        @NotBlank(message = "Nome obrigatório")
        @Size(max = 100, message = "Nome longo")
        String name,

        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal price,

        @NotBlank(message = "Categoria é obrigatória")
        String category,

        Integer stock,

        @Size(max = 500, message = "descrição longa")
        String description,

        @NotBlank(message = "Imagem é obrigatório")
        MultipartFile image
) {
}
