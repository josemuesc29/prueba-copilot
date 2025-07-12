package com.imaginamos.farmatodo.model.order;

public class GetLastStatusOrderProvider {

    private String code;
    private String message;
    private Long data;

    public GetLastStatusOrderProvider() {
    }

    public GetLastStatusOrderProvider(String code, String message, Long data) {
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

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GetLastStatusOrderProvider{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
