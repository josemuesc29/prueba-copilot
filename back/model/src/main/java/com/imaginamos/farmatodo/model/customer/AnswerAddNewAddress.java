package com.imaginamos.farmatodo.model.customer;

public class AnswerAddNewAddress {

    private Boolean confirmation;
    private String message;
    private Address address;

    public AnswerAddNewAddress() {
    }

    public Boolean getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
