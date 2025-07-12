package com.imaginamos.farmatodo.model.customer;

public class CustomerChangePasswordV2 {
    private String idCustomer;
    private String validationCode;
    private String password;
    private String token;
    private String idCustomerWebSafe;
    private String TokenIdWebSafe;

    public String getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(String idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
    }

    public String getTokenIdWebSafe() {
        return TokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        TokenIdWebSafe = tokenIdWebSafe;
    }

    @Override
    public String toString() {
        return "CustomerChangePasswordV2{" +
                "idCustomer='" + idCustomer + '\'' +
                ", validationCode='" + validationCode + '\'' +
                ", token='" + token + '\'' +
                ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
                ", TokenIdWebSafe='" + TokenIdWebSafe + '\'' +
                '}';
    }
}
