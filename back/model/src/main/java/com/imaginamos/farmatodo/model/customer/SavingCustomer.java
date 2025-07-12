package com.imaginamos.farmatodo.model.customer;


public class SavingCustomer {
    private Long customerId;
    private Double primeSaving;
    private UpdateTypeSavingEnum updateTypeSavingEnum;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Double getPrimeSaving() {
        return primeSaving;
    }

    public void setPrimeSaving(Double primeSaving) {
        this.primeSaving = primeSaving;
    }

    public UpdateTypeSavingEnum getUpdateTypeSavingEnum() {
        return updateTypeSavingEnum;
    }

    public void setUpdateTypeSavingEnum(UpdateTypeSavingEnum updateTypeSavingEnum) {
        this.updateTypeSavingEnum = updateTypeSavingEnum;
    }
}
