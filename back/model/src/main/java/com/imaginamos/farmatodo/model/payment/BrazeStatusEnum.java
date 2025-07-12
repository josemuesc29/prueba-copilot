package com.imaginamos.farmatodo.model.payment;

public enum BrazeStatusEnum {

    CREATED("CREATED"),
    ASSIGNED("ASSIGNED"),
    BILLED("BILLED"),
    PICKING("PICKING"),
    DELIVERY("DELIVERY"),
    FINISH("FINISH"),
    CANCEL("CANCEL"),
    RETURNED("RETURNED"),
    RETURNED_SUCCESS("RETURNED_SUCCESS");

    private final String value;


    BrazeStatusEnum(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }


}
