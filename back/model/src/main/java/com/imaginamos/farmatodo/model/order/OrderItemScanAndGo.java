package com.imaginamos.farmatodo.model.order;


import com.imaginamos.farmatodo.model.product.Item;
import org.json.simple.JSONObject;

public class OrderItemScanAndGo {
    private Item item;
    private int quantitySold;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

}
