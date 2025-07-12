package com.imaginamos.farmatodo.networking.models.braze;

public class CreateUserOnBrazeRequest {
    private String email;
    private String document;
    private String phone;

    public CreateUserOnBrazeRequest(String email) {
        this.email = email;
    }

    public CreateUserOnBrazeRequest(String email, String document, String phone) {
        this.email = email;
        this.document = document;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocument() { return document; }

    public void setDocument(String document) {
        this.email = document;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}