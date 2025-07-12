package com.imaginamos.farmatodo.model.customer;

/**
 * Created by ccrodriguez
 */
public class RequestEmailValidate {
    private String email;

    public RequestEmailValidate(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
