package com.imaginamos.farmatodo.model.item;

public class StoreItemPrice {
    private long store;
    private Double price;


    public long getStore() {
        return store;
    }

    public void setStore(long store) {
        this.store = store;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "StoreItemPrice{" +
                "store=" + store +
                ", price=" + price +
                '}';
    }
}
