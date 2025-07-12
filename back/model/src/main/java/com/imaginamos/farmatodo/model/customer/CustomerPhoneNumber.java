package com.imaginamos.farmatodo.model.customer;

import java.io.Serializable;

public class CustomerPhoneNumber implements Serializable {

    private String phoneNumber;
    private String numberType;

    public CustomerPhoneNumber() {
    }

    public CustomerPhoneNumber(String phoneNumber, String numberType) {
        this.phoneNumber = phoneNumber;
        this.numberType = numberType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNumberType() {
        return numberType;
    }

    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }
}
