package com.br.api.lanchonete.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Type;

public record RegisterDTO (

        @NotBlank
        @Size(min = 3, max = 15, message = "Username deve ter mais 3 caracteres e menos de 15")
        String username,

        @NotBlank
        @Email(message = "Email deve ser válido")
        String email,

        @NotBlank
        @Size(min = 3, max = 50, message = "Username deve ter mais 3 caracteres e menos de 50")
        String name,

        @NotBlank
        @Size(min = 8, max = 20, message = "Senha deve possuir mais de 8 e no maximo 20 caracteres")
        String password,
        UserRole role
){ }
