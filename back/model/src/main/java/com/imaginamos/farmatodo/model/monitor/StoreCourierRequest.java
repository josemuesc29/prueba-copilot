package com.imaginamos.farmatodo.model.monitor;

import java.util.List;

public class StoreCourierRequest {
    private List<StoreCourier> storesByCourier;

    public List<StoreCourier> getStoresByCourier() {
        return storesByCourier;
    }

    public void setStoresByCourier(List<StoreCourier> storesByCourier) {
        this.storesByCourier = storesByCourier;
    }
}
