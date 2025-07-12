package com.imaginamos.farmatodo.model.provider;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.product.ItemOrder;

import java.util.List;

/**
 * Created by icruz on 06/07/2018
 */
public class ProviderOrder {
    private Long id;
    private String name;
    private String email;
    private int deliveryPrice;
    private List<ItemAlgolia> items;
    private int deliveryStatus;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public int getDeliveryPrice() { return deliveryPrice; }

    public void setDeliveryPrice(int deliveryPrice) { this.deliveryPrice = deliveryPrice; }

    public List<ItemAlgolia> getItems() { return items; }

    public void setItems(List<ItemAlgolia> items) { this.items = items; }

    public int getDeliveryStatus() { return deliveryStatus; }

    public void setDeliveryStatus(int deliveryStatus) { this.deliveryStatus = deliveryStatus; }
}
