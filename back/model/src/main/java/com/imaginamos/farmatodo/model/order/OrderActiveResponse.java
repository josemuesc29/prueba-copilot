package com.imaginamos.farmatodo.model.order;

public class OrderActiveResponse {
    private long idOrder;
    private Boolean isActive;

    public long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(long idOrder) {
        this.idOrder = idOrder;
    }

    public boolean isActive(Boolean b) {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
