package com.imaginamos.farmatodo.model.item;

import java.util.List;

public class AddDeliveryOrderItemRequest {

    private String urlPrescription;
    private List<OpticalItemFilter> opticalItemFilterList;
    private List<Integer> nearbyStores;

    public AddDeliveryOrderItemRequest() {
    }

    public AddDeliveryOrderItemRequest(List<OpticalItemFilter> opticalItemFilterList) {
        this.opticalItemFilterList = opticalItemFilterList;
    }

    public List<OpticalItemFilter> getOpticalItemFilterList() {
        return opticalItemFilterList;
    }

    public void setOpticalItemFilterList(List<OpticalItemFilter> opticalItemFilterList) {
        this.opticalItemFilterList = opticalItemFilterList;
    }

    public String getUrlPrescription() {
        return urlPrescription;
    }

    public void setUrlPrescription(String urlPrescription) {
        this.urlPrescription = urlPrescription;
    }

    public List<Integer> getNearbyStores() {
        return nearbyStores;
    }

    public void setNearbyStores(List<Integer> nearbyStores) {
        this.nearbyStores = nearbyStores;
    }
}
