package com.imaginamos.farmatodo.model.OptimalRoute;

import java.util.List;

public class ShoppingCartLocator {
    private List<ItemDomain> items;

    public ShoppingCartLocator(List<ItemDomain> items) {
        this.items = items;
    }

    public ShoppingCartLocator() {
    }

    public List<ItemDomain> getItems() {
        return items;
    }

    public void setItems(List<ItemDomain> items) {
        this.items = items;
    }
}
