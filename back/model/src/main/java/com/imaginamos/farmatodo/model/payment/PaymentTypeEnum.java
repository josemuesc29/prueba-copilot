package com.imaginamos.farmatodo.model.payment;

public enum PaymentTypeEnum {
    EFECTIVO(1L),
    DATAFONOS(2L),
    TRANSACCIONES_EN_LINEA(3L),
    PSE(6L);

    private final Long id;

    PaymentTypeEnum(Long id) {
        this.id = id;
    }

    /**
     * find payment type enum by id
     * @param id
     * @return
     */
    public static PaymentTypeEnum find(Long id) {
        for (PaymentTypeEnum record : values()) {
            if (record.getId().equals(id)) {
                return record;
            }
        }
        return null;
    }

    /**
     * @return
     */
    public Long getId() {
        return id;
    }
}
