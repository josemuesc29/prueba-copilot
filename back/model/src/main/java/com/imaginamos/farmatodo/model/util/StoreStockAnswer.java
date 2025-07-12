package com.imaginamos.farmatodo.model.util;

import com.imaginamos.farmatodo.model.product.StoreInformation;

public class StoreStockAnswer {
    private boolean confirmation;
    private StoreInformation storeInformation;
    private String message;


    public boolean isConfirmation() {
        return confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    public StoreInformation getStoreInformation() {
        return storeInformation;
    }

    public void setStoreInformation(StoreInformation storeInformation) {
        this.storeInformation = storeInformation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
