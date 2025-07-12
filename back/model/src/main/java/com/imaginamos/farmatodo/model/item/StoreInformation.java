package com.imaginamos.farmatodo.model.item;

/**
 * Created by JPuentes on 22/06/2018.
 */
public class StoreInformation {

    private int storeId;
    private Double offerPrice;
    private String offerDesc;
    private String offerText;
    private Double fullPrice;
    private int itemId;
    private int stock;

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getOfferDesc() {
        return offerDesc;
    }

    public void setOfferDesc(String offerDesc) {
        this.offerDesc = offerDesc;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public Double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(Double fullPrice) {
        this.fullPrice = fullPrice;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "StoreInformation{" +
                "storeId=" + storeId +
                ", offerPrice=" + offerPrice +
                ", offerDesc=" + offerDesc +
                ", offerText='" + offerText + '\'' +
                ", fullPrice=" + fullPrice +
                ", itemId=" + itemId +
                ", stock=" + stock +
                '}';
    }
}
