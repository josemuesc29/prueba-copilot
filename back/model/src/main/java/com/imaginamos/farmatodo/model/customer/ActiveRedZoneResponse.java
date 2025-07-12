package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName;

public class ActiveRedZoneResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ActiveRedZoneData data;

    // Getters y setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ActiveRedZoneData getData() {
        return data;
    }

    public void setData(ActiveRedZoneData data) {
        this.data = data;
    }
}
