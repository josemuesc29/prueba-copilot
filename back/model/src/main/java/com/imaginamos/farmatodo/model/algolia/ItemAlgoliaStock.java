package com.imaginamos.farmatodo.model.algolia;

import java.io.Serializable;
import java.util.logging.Logger;

public class ItemAlgoliaStock implements Serializable {
    private static final Logger LOG = Logger.getLogger(ItemAlgoliaStock.class.getName());

    private String id;
    private Integer stock;

    private Integer totalStock;

    private String objectID;

    public ItemAlgoliaStock() {
    }

    public ItemAlgoliaStock(String id, Integer stock, Integer totalStock, String objectID) {
        this.id = id;
        this.stock = stock;
        this.totalStock = totalStock;
        this.objectID=objectID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
