package com.imaginamos.farmatodo.networking.models.braze;

public class EmailFromTemplate {
    private String email;

    public EmailFromTemplate() {
    }

    public EmailFromTemplate(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
