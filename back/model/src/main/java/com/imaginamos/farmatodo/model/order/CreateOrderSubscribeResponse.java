package com.imaginamos.farmatodo.model.order;

public class CreateOrderSubscribeResponse {
    private String code;
    private String message;
    private CreateOrderSubscribeData data;

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

    public CreateOrderSubscribeData getData() {
        return data;
    }

    public void setData(CreateOrderSubscribeData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CreateOrderSubscribeResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data.toString() +
                '}';
    }
}
