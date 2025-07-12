package com.imaginamos.farmatodo.model.algolia;

public class CustomerCallCenterData {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CustomerDataResponse{" +
                "email='" + email + '\'' +
                '}';
    }
}
