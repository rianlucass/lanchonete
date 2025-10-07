package com.br.api.lanchonete.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class RequestUserDTO {
    private String username;
    private String password;
    private LocalDate creationDate;
}
