package com.imaginamos.farmatodo.networking.models.braze;

public class NotificationAndEmailBrazeRequest {

    private String email;
    private NotificationBrazeRequest notifications;

    public NotificationAndEmailBrazeRequest() {
    }

    public NotificationAndEmailBrazeRequest(String email, NotificationBrazeRequest notifications) {
        this.email = email;
        this.notifications = notifications;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NotificationBrazeRequest getNotifications() {
        return notifications;
    }

    public void setNotifications(NotificationBrazeRequest notifications) {
        this.notifications = notifications;
    }


}
