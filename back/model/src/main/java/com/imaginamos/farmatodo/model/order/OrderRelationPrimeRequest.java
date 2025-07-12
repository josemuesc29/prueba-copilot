package com.imaginamos.farmatodo.model.order;

public class OrderRelationPrimeRequest {

    private Long orderIdExpress;
    private Long orderIdPrime;

    public OrderRelationPrimeRequest() {
    }

    public OrderRelationPrimeRequest(Long orderIdExpress, Long orderIdPrime) {
        this.orderIdExpress = orderIdExpress;
        this.orderIdPrime = orderIdPrime;
    }

    public Long getOrderIdExpress() {
        return orderIdExpress;
    }

    public void setOrderIdExpress(Long orderIdExpress) {
        this.orderIdExpress = orderIdExpress;
    }

    public Long getOrderIdPrime() {
        return orderIdPrime;
    }

    public void setOrderIdPrime(Long orderIdPrime) {
        this.orderIdPrime = orderIdPrime;
    }
}
