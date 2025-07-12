package com.imaginamos.farmatodo.model.item;

public class ItemPriceUpload {
    private long item;
    private long store;
    private int fullPrice;

    public long getItem() {
        return item;
    }

    public void setItem(long item) {
        this.item = item;
    }

    public long getStore() {
        return store;
    }

    public void setStore(long store) {
        this.store = store;
    }

    public int getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(int fullPrice) {
        this.fullPrice = fullPrice;
    }


    @Override
    public String toString() {
        return "ItemPriceUpload{" +
                "item=" + item +
                ", store=" + store +
                ", fullPrice=" + fullPrice +
                '}';
    }
}
