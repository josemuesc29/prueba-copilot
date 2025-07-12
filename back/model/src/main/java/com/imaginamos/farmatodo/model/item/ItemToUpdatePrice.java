package com.imaginamos.farmatodo.model.item;

import java.util.List;

public class ItemToUpdatePrice {
    private long item;
    private List<StoreItemPrice> storePriceList;


    public long getItem() {
        return item;
    }

    public void setItem(long item) {
        this.item = item;
    }

    public List<StoreItemPrice> getStorePriceList() {
        return storePriceList;
    }

    public void setStorePriceList(List<StoreItemPrice> storePriceList) {
        this.storePriceList = storePriceList;
    }

    @Override
    public String toString() {
        return "ItemToUpdatePrice{" +
                "item=" + item +
                ", storePriceList=" + storePriceList +
                '}';
    }
}
