package com.imaginamos.farmatodo.model.order;

public class CancelOrderCourierRes {
    private String code;
    private String message;
    private ReturnCouponRes data;

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

    public ReturnCouponRes getData() {
        return data;
    }

    public void setData(ReturnCouponRes data) {
        this.data = data;
    }
}
