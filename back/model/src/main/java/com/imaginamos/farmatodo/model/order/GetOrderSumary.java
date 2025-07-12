package com.imaginamos.farmatodo.model.order;

public class GetOrderSumary {
    private String code;
    private String message;
    private GetOrderSummaryData data;

    public GetOrderSumary() {
    }

    public GetOrderSumary(String code, String message, GetOrderSummaryData data) {
        this.code = code;
        this.message = message;
        this.data = data;
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

    public GetOrderSummaryData getData() {
        return data;
    }

    public void setData(GetOrderSummaryData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GetOrderSumary{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
