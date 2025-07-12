package com.imaginamos.farmatodo.model.coupon;

public class GetCouponByNameResponse {

    private boolean success;
    private String message;
    private int status_code;
    private GetCouponByNameData data;

    public GetCouponByNameResponse(boolean success, String message, int status_code, GetCouponByNameData data) {
        this.success = success;
        this.message = message;
        this.status_code = status_code;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public GetCouponByNameData getData() {
        return data;
    }

    public void setData(GetCouponByNameData data) {
        this.data = data;
    }
}
