package com.imaginamos.farmatodo.model.stock;

import com.imaginamos.farmatodo.model.item.ItemToUpdatePrice;

import java.util.List;

/**
 * Created by JPuentes on 23/07/2018.
 */
public class UpdateStockItemRequest {

   private Long item;
   private List<StoreStockv2> stock;

    public Long getItem() {
        return item;
    }

    public void setItem(Long item) {
        this.item = item;
    }

    public List<StoreStockv2> getStock() {
        return stock;
    }

    public void setStock(List<StoreStockv2> stock) {
        this.stock = stock;
    }
}
