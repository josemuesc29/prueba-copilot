package com.imaginamos.farmatodo.model.product;

public class ItemQuery {
    private String itemStore;

    public ItemQuery(String itemStore) {
        this.itemStore = itemStore;
    }

    public String getItemStore() { return itemStore; }

    public void setItemStore(String itemStore) { this.itemStore = itemStore; }

    @Override
    public String toString() {
        return "ItemQuery{" +
                "itemStore='" + itemStore + '\'' +
                '}';
    }
}
