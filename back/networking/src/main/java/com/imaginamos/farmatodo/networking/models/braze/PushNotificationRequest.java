package com.imaginamos.farmatodo.networking.models.braze;

public class PushNotificationRequest {

    private int phone;
    private String email;
    private String title;
    private String body;
    private String custom_uri;
    private Long orderId;
    private String status;
    private String messengerName;

    public PushNotificationRequest(String email, String title, String body, String custom_uri) {
        this.email = email;
        this.title = title;
        this.body = body;
        this.custom_uri = custom_uri;
    }

    public PushNotificationRequest(
            String email, String title, String body, String custom_uri, Long orderId, String status,
            String messengerName) {
        this.email = email;
        this.title = title;
        this.body = body;
        this.custom_uri = custom_uri;
        this.orderId = orderId;
        this.status = status;
        this.messengerName = messengerName;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCustom_uri() {
        return custom_uri;
    }

    public void setCustom_uri(String custom_uri) {
        this.custom_uri = custom_uri;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessengerName() {
        return messengerName;
    }

    public void setMessengerName(String messengerName) {
        this.messengerName = messengerName;
    }
}
