package com.imaginamos.farmatodo.model.order;

public class ClientResponse {
    private Integer status;
    private String message;
    private Object data;

    public ClientResponse() {}

    public ClientResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }

    public void setData(Object data) { this.data = data; }
}
