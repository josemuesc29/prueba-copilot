package com.imaginamos.farmatodo.model.OptimalRoute;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.List;

public class OptimalRoutePopUpResponseData {
    private int radio;
    private String cityId;
    private int customerId;
    private int nearestStoreOpen;
    private String transferReason;
    private double distance;
    private int bestStore;
    private String calculatedBy;
    private int percentageStock;
    private List<ItemPercentageCompleteness> itemsToSubstitutePercentage;
    private List<ItemPercentageCompleteness> itemsPercentageCompleteness;

    private List<Integer> itemsToSubstitute;
    private String uuid;

    public OptimalRoutePopUpResponseData() {
    }

    public OptimalRoutePopUpResponseData(
            int radio, String cityId, int customerId, int nearestStoreOpen,
            String transferReason, double distance, int bestStore,
            String calculatedBy, int percentageStock,
            List<ItemPercentageCompleteness> itemsToSubstitutePercentage,
            List<ItemPercentageCompleteness> itemsPercentageCompleteness,
            List<Integer> itemsToSubstitute, String uuid
    ) {
        this.radio = radio;
        this.cityId = cityId;
        this.customerId = customerId;
        this.nearestStoreOpen = nearestStoreOpen;
        this.transferReason = transferReason;
        this.distance = distance;
        this.bestStore = bestStore;
        this.calculatedBy = calculatedBy;
        this.percentageStock = percentageStock;
        this.itemsToSubstitutePercentage = itemsToSubstitutePercentage;
        this.itemsPercentageCompleteness = itemsPercentageCompleteness;
        this.itemsToSubstitute = itemsToSubstitute;
        this.uuid = uuid;
    }

    public int getRadio() {
        return radio;
    }

    public void setRadio(int radio) {
        this.radio = radio;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getNearestStoreOpen() {
        return nearestStoreOpen;
    }

    public void setNearestStoreOpen(int nearestStoreOpen) {
        this.nearestStoreOpen = nearestStoreOpen;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getBestStore() {
        return bestStore;
    }

    public void setBestStore(int bestStore) {
        this.bestStore = bestStore;
    }

    public String getCalculatedBy() {
        return calculatedBy;
    }

    public void setCalculatedBy(String calculatedBy) {
        this.calculatedBy = calculatedBy;
    }

    public int getPercentageStock() {
        return percentageStock;
    }

    public void setPercentageStock(int percentageStock) {
        this.percentageStock = percentageStock;
    }

    public List<ItemPercentageCompleteness> getItemsToSubstitutePercentage() {
        return itemsToSubstitutePercentage;
    }

    public void setItemsToSubstitutePercentage(List<ItemPercentageCompleteness> itemsToSubstitutePercentage) {
        this.itemsToSubstitutePercentage = itemsToSubstitutePercentage;
    }

    public List<Integer> getItemsToSubstitute() {
        return itemsToSubstitute;
    }

    public void setItemsToSubstitute(List<Integer> itemsToSubstitute) {
        this.itemsToSubstitute = itemsToSubstitute;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<ItemPercentageCompleteness> getItemsPercentageCompleteness() {
        return itemsPercentageCompleteness;
    }

    public void setItemsPercentageCompleteness(List<ItemPercentageCompleteness> itemsPercentageCompleteness) {
        this.itemsPercentageCompleteness = itemsPercentageCompleteness;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
