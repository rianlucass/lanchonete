package com.br.api.lanchonete.domain.user;

public record AuthenticationDTO(
        String username,
        String password
) {
}
