package com.imaginamos.farmatodo.model.customer;

public enum SubscriptionPrimeTypeEnum {
    YEAR(1L),
    MONTH(2L);

    private final Long id;

    private SubscriptionPrimeTypeEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }
}
