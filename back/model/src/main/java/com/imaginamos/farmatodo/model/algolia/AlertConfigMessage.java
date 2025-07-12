package com.imaginamos.farmatodo.model.algolia;

import java.util.ArrayList;
import java.util.List;

public class AlertConfigMessage {
    private List<String> phoneNumbers = new ArrayList<>();
    private String message;

    public AlertConfigMessage() {
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AlertConfigMessage{" +
                "phoneNumbers=" + phoneNumbers +
                ", message='" + message + '\'' +
                '}';
    }
}
