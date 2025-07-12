package com.imaginamos.farmatodo.model.OptimalRoute;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class OptimalRoutePopUpResponseDomain {
    private String code;
    private String message;
    private OptimalRoutePopUpResponseData data;

    public OptimalRoutePopUpResponseDomain(
            String code, String message,
            OptimalRoutePopUpResponseData data
    ) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public OptimalRoutePopUpResponseDomain() {
    }

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

    public OptimalRoutePopUpResponseData getData() {
        return data;
    }

    public void setData(OptimalRoutePopUpResponseData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
