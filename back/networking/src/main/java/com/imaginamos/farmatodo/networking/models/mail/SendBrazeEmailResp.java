package com.imaginamos.farmatodo.networking.models.mail;

public class SendBrazeEmailResp {
    private Boolean confirmation;
    private long status;
    private String message;


    public SendBrazeEmailResp() {
    }

    public Boolean getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
