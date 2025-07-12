package com.imaginamos.farmatodo.model.customer;


public class CustomerCreditCardGateway {
    private String national;
    private String international;

    public CustomerCreditCardGateway() {
    }

    public String getNational() {
        return national;
    }

    public void setNational(String national) {
        this.national = national;
    }

    public String getInternational() {
        return international;
    }

    public void setInternational(String international) {
        this.international = international;
    }
}
