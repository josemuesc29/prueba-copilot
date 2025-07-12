package com.imaginamos.farmatodo.networking.models.growthbook;

public class NotificationConfig {
    private int status;
    private String message;
    private String title;

    // Constructor
    public NotificationConfig(int status, String message, String title) {
        this.status = status;
        this.message = message;
        this.title = title;
    }

    // Getters y Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
