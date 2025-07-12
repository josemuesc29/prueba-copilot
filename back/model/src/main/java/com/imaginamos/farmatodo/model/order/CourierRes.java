package com.imaginamos.farmatodo.model.order;

public class CourierRes {

    private Long id;
    private String name;
    private Integer status;
    private Integer orderRequired;
    private Integer tokenRequired;
    private Long    cufDelivery;
    private Integer hoursNumber;
    private Integer paymentTypeRequired;
    private Integer principal;
    private String  tokenFunction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOrderRequired() {
        return orderRequired;
    }

    public void setOrderRequired(Integer orderRequired) {
        this.orderRequired = orderRequired;
    }

    public Integer getTokenRequired() {
        return tokenRequired;
    }

    public void setTokenRequired(Integer tokenRequired) {
        this.tokenRequired = tokenRequired;
    }

    public Long getCufDelivery() {
        return cufDelivery;
    }

    public void setCufDelivery(Long cufDelivery) {
        this.cufDelivery = cufDelivery;
    }

    public Integer getHoursNumber() {
        return hoursNumber;
    }

    public void setHoursNumber(Integer hoursNumber) {
        this.hoursNumber = hoursNumber;
    }

    public Integer getPaymentTypeRequired() {
        return paymentTypeRequired;
    }

    public void setPaymentTypeRequired(Integer paymentTypeRequired) {
        this.paymentTypeRequired = paymentTypeRequired;
    }

    public Integer getPrincipal() {
        return principal;
    }

    public void setPrincipal(Integer principal) {
        this.principal = principal;
    }

    public String getTokenFunction() {
        return tokenFunction;
    }

    public void setTokenFunction(String tokenFunction) {
        this.tokenFunction = tokenFunction;
    }
}
