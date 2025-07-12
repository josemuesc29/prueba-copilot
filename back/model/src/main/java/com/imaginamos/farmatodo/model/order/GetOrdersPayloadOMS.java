package com.imaginamos.farmatodo.model.order;

public class GetOrdersPayloadOMS {

    private long idCustomer;
    private String cursor;


    // Getter
    public long getIdCustomer() {
        return idCustomer;
    }

    // Setter
    public void setIdCustomer(long idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "Customer{idCustomer=" + idCustomer + "}";
    }
}
