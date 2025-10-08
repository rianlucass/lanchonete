package com.br.api.lanchonete.domain.user;

public record RegisterDTO (
        String username,
        String email,
        String name,
        String password,
        UserRole role
){ }
