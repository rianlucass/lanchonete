package com.br.api.lanchonete.domain.stock;

public enum MovementType {
    ENTRY("Entry"),
    EXIT("Exit"),
    ADJUSTMENT("Adjustment"),
    LOSS("Loss");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
