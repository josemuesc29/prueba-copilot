package com.imaginamos.farmatodo.model.algolia;


import java.io.Serializable;

public class PaymentMessageConfig implements Serializable {

    public String event;
    public String mainMessage;
    public String secondMessage;

    @Override
    public String toString() {
        return "PaymentMessageConfig{" +
                "event='" + event + '\'' +
                ", mainMessage='" + mainMessage + '\'' +
                ", secondMessage='" + secondMessage + '\'' +
                '}';
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMainMessage() {
        return mainMessage;
    }

    public void setMainMessage(String mainMessage) {
        this.mainMessage = mainMessage;
    }

    public String getSecondMessage() {
        return secondMessage;
    }

    public void setSecondMessage(String secondMessage) {
        this.secondMessage = secondMessage;
    }
}
