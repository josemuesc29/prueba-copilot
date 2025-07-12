package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 23/11/2018.
 */
public class GetAdvisedItemResponse {
    private String statusCode;
    private String status;
    private AdvisedItem advisedItems;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AdvisedItem getAdvisedItems() {
        return advisedItems;
    }

    public void setAdvisedItems(AdvisedItem advisedItems) {
        this.advisedItems = advisedItems;
    }
}
