package com.imaginamos.farmatodo.model.order;

public class AmplitudeEventsResponse {

    private String status;
    private String message;
    private DataEventResponseAmplitude data;

    public AmplitudeEventsResponse() {
    }

    public AmplitudeEventsResponse(String code, String message, DataEventResponseAmplitude data) {
        this.status = code;
        this.message = message;
        this.data = data;
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

    public DataEventResponseAmplitude getData() {
        return data;
    }

    public void setData(DataEventResponseAmplitude data) {
        this.data = data;
    }
}
