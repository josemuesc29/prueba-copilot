package com.imaginamos.farmatodo.model.product;


import java.util.List;


public class ItemMarketplaceVariant {

    private String item;
    private List<ItemMarketplaceVariantDetail> variantDetails;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public List<ItemMarketplaceVariantDetail> getVariantDetails() {
        return variantDetails;
    }

    public void setVariantDetails(List<ItemMarketplaceVariantDetail> variantDetails) {
        this.variantDetails = variantDetails;
    }
}