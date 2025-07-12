package com.imaginamos.farmatodo.networking.models.braze;

public class CreateUserOnBrazeResponse {
    private Long status;
    private Boolean confirmation;
    private String message;
    private DataBraze data;

    public Boolean getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBraze getData() {
        return data;
    }

    public void setData(DataBraze data) {
        this.data = data;
    }
}
