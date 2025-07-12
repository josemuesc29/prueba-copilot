package com.imaginamos.farmatodo.networking.models.growthbook;


import java.util.List;

public class NotificationOrderConfig {
    private List<NotificationConfig> config;

    // Constructor
    public NotificationOrderConfig(List<NotificationConfig> config) {
        this.config = config;
    }

    // Getters y Setters
    public List<NotificationConfig> getConfig() {
        return config;
    }

    public void setConfig(List<NotificationConfig> config) {
        this.config = config;
    }
}

