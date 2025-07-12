package com.imaginamos.farmatodo.model.product;

import java.util.ArrayList;
import java.util.List;

public class ItemStock {
    private List<ItemStockValue> items;

    public ItemStock(){
        items = new ArrayList<>();
    }

    public List<ItemStockValue> getItems() { return items; }

    public void setItems(List<ItemStockValue> items) { this.items = items; }

    @Override
    public String toString() {
        return "ItemStock{" +
                "items=" + items +
                '}';
    }

    public static class ItemStockValue{
        private Long id;
        private int stock;

        public ItemStockValue(){}

        public Long getId() { return id; }

        public void setId(Long id) { this.id = id; }

        public int getStock() { return stock; }

        public void setStock(int stock) { this.stock = stock; }

        @Override
        public String toString() {
            return "ItemStockValue{" +
                    "id=" + id +
                    ", stock=" + stock +
                    '}';
        }
    }
}
