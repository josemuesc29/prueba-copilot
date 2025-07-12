package com.imaginamos.farmatodo.model.customer;

public enum DocumentType {

    CEDULA_CIUDADANIA(1L);

    private final Long id;

    DocumentType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
