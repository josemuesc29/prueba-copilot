package com.imaginamos.farmatodo.model.algolia;

public class DeliveryFast {

    private boolean active;
    private int minDeliveryTime;
    private String message;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMinDeliveryTime() {
        return minDeliveryTime;
    }

    public void setMinDeliveryTime(int minDeliveryTime) {
        this.minDeliveryTime = minDeliveryTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
