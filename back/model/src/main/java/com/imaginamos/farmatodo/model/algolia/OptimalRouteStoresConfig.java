package com.imaginamos.farmatodo.model.algolia;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class OptimalRouteStoresConfig {
    private StoreOptimalRoute stores;
    private boolean valid;

    public StoreOptimalRoute getStores() {return stores;}

    public void setStores(StoreOptimalRoute stores) {this.stores = stores;}

    public boolean isValid() {return valid;}

    public void setValid(boolean valid) {this.valid = valid;}

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
