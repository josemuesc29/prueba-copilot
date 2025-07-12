package com.imaginamos.farmatodo.model.customer;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class SavingsPrimeGeneral {
    @Id
    private String  id;
    @Index
    private Long customerId;
    private double primeSaving;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public double getPrimeSaving() {
        return primeSaving;
    }

    public void setPrimeSaving(double primeSaving) {
        this.primeSaving = primeSaving;
    }
}

