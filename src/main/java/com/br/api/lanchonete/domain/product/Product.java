package com.br.api.lanchonete.domain.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Category category;

    private Boolean active;
    private Integer stock;
    private String description;
    private String imageURL;

}
