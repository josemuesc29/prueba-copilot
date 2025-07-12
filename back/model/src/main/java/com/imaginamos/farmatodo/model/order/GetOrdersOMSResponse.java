package com.imaginamos.farmatodo.model.order;

import java.util.ArrayList;

public class GetOrdersOMSResponse {

    private ArrayList<ReleaseOrder> previousOrders;

    private ArrayList<ReleaseOrder> activeOrders;

    private String nextPageToken;

    public ArrayList<ReleaseOrder> getPreviousOrders() {
        return previousOrders;
    }

    public void setPreviousOrders(ArrayList<ReleaseOrder> releaseOrders) {
        this.previousOrders = releaseOrders;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public ArrayList<ReleaseOrder> getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(ArrayList<ReleaseOrder> activeOrders) {
        this.activeOrders = activeOrders;
    }
}
