package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.internal.LinkedTreeMap;

public class CartItem {
    private String name;
    private String sku;
    private int quantity;
    private Double price;
    private String category;
    private LinkedTreeMap<String, String> attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LinkedTreeMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(LinkedTreeMap<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
