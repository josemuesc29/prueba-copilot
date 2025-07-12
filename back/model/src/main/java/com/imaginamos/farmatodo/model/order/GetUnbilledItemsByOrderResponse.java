package com.imaginamos.farmatodo.model.order;

public class GetUnbilledItemsByOrderResponse {

    private String code;
    private String message;
    private GetUnbilledItemsByOrderResponseData data;

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

    public GetUnbilledItemsByOrderResponseData getData() {
        return data;
    }

    public void setData(GetUnbilledItemsByOrderResponseData data) {
        this.data = data;
    }
}
