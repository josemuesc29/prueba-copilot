package com.imaginamos.farmatodo.networking.models.amplitude;

public class ClassificationBusiness<T>{

    private Boolean confirmation;
    private String message;
    private T data;

    public Boolean getConfirmation() { return confirmation; }

    public void setConfirmation(Boolean confirmation) { this.confirmation = confirmation; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }

    public void setData(T data) { this.data = data; }
}
