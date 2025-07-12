package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 18/10/2018.
 */
public class AddRecommendedItemResponse {

    private String status;
    private Integer statusCode;
    private String message;

    public AddRecommendedItemResponse(String status, Integer statusCode, String message) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
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
}
