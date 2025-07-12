package com.imaginamos.farmatodo.backend.customer.async.models;

import java.util.ArrayList;
import java.util.List;

public class DatasourcesIds {
    private List<String> favoriteIds = new ArrayList<>();
    private List<String> purchasesIds = new ArrayList<>();
    private List<String> viewedIds = new ArrayList<>();

    public List<String> getFavoriteIds() {
        return favoriteIds;
    }

    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
    }

    public List<String> getPurchasesIds() {
        return purchasesIds;
    }

    public void setPurchasesIds(List<String> purchasesIds) {
        this.purchasesIds = purchasesIds;
    }

    public List<String> getViewedIds() {
        return viewedIds;
    }

    public void setViewedIds(List<String> viewedIds) {
        this.viewedIds = viewedIds;
    }
}
