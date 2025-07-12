package com.imaginamos.farmatodo.networking.models.amplitude;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class AmplitudeSessionRequest {

    private Long orderId;
    private Long sessionId;

    public Long getOrderId() { return orderId; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getSessionId() { return sessionId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
