package com.imaginamos.farmatodo.model.order;

public class GetOrderStopsResponse {

    private String code;
    private String message;
    private GetOrderStopsResponseData data;

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

    public GetOrderStopsResponseData getData() {
        return data;
    }

    public void setData(GetOrderStopsResponseData data) {
        this.data = data;
    }

}
