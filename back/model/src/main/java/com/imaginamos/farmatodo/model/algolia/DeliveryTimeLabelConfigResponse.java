package com.imaginamos.farmatodo.model.algolia;

public class DeliveryTimeLabelConfigResponse {

    private String status;
    private Integer statusCode;
    private String message;
    private DeliveryTimeLabelConfig data;

    public DeliveryTimeLabelConfigResponse(String status, Integer statusCode, String message, DeliveryTimeLabelConfig data) {
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

    public DeliveryTimeLabelConfig getData() {
        return data;
    }

    public void setData(DeliveryTimeLabelConfig data) {
        this.data = data;
    }

}
