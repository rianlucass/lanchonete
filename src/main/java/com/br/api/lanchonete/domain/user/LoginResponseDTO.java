package com.br.api.lanchonete.domain.user;

public record LoginResponseDTO(
        String token,
        UserRole role
) {
}
