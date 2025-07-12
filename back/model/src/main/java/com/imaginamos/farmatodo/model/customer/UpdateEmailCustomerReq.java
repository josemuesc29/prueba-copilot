package com.imaginamos.farmatodo.model.customer;

public class UpdateEmailCustomerReq {
    private String idCustomerWebSafe;
    private String token;
    private String tokenIdWebSafe;
    private String newEmail;

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public boolean isValid(){
        return idCustomerWebSafe != null 
                && !idCustomerWebSafe.isEmpty() 
                && token != null && !token.isEmpty()
                && tokenIdWebSafe != null && !tokenIdWebSafe.isEmpty()
                && newEmail != null && !newEmail.isEmpty();
    }
}
