package com.imaginamos.farmatodo.model.order;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class Bck3EventResponse {
    private Bck3EventResponseCode code;
    private String message;

    public Bck3EventResponse() {
    }

    public Bck3EventResponseCode getCode() {
        return code;
    }

    public void setCode(Bck3EventResponseCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
