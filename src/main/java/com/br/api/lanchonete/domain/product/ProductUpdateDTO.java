package com.br.api.lanchonete.domain.product;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public record ProductUpdateDTO(
        Optional<String> name,
        Optional<Double> price,
        Optional<String> category,
        Optional<String> description,
        Optional<Integer> stock,
        MultipartFile image
) {}
