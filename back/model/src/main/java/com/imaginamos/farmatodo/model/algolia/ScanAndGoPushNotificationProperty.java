package com.imaginamos.farmatodo.model.algolia;

public class ScanAndGoPushNotificationProperty {

    private String title;

    private String message;

    private Integer timeToPushInHours;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTimeToPushInHours() {
        return timeToPushInHours;
    }

    public void setTimeToPushInHours(Integer timeToPushInHours) {
        this.timeToPushInHours = timeToPushInHours;
    }
}
