package com.imaginamos.farmatodo.networking.models.braze;

public class GetUserByEmailBrazeResponse {
    private Boolean confirmation;
    private String message;
    private DataBrazeUUID data;

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

    public DataBrazeUUID getData() {
        return data;
    }

    public void setData(DataBrazeUUID data) {
        this.data = data;
    }
}
