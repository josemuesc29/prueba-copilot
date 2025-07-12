package com.imaginamos.farmatodo.networking.talonone.model;

public class TrackEventResponse {
    private  String status;
    private  String message;
    private  Object body;

    public TrackEventResponse() {
    }

    public TrackEventResponse(String status, String message, Object body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
