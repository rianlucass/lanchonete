package com.br.api.lanchonete.domain.product;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

public record ProductUpdateDTO(
        Optional<String> name,
        Optional<BigDecimal> price,
        Optional<Category> category,
        Optional<String> description,
        Optional<Integer> stock,
        MultipartFile image
) {}
