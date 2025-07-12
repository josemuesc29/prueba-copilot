package com.imaginamos.farmatodo.model.customer;

public class ValidateCustomerOracle {
    private String email;
    private Boolean validation;

    private Long id;

    public ValidateCustomerOracle() {
    }

    public ValidateCustomerOracle(String email, Boolean validation) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getValidation() {
        return validation;
    }

    public void setValidation(Boolean validation) {
        this.validation = validation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
