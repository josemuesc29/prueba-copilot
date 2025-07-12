package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class CustomerCallResponse {
    private String code;
    private String message;
    private List<CustomerCallResponseData> data;

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

    public List<CustomerCallResponseData> getData() {
        return data;
    }

    public void setData(List<CustomerCallResponseData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CustomerCallResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
