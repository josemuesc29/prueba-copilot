package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 18/10/2018.
 */
public class GetRecommendedItemResponse {

    private String status;
    private Integer statusCode;
    private String message;
    private Object data;

    public GetRecommendedItemResponse(String status, Integer statusCode, String message, Object data) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
