package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class DeliveryOrderProviderOms {
    private String idDeliveryOrderProvider;
    private long id;
    private String deliveryKey;
    private String name;
    private String email;
    private int deliveryPrice;
    private String deliveryTimeOptics;
    private List<DeliveryOrderItemOms> itemList;
    private int quantityItem;
    private long deliveryStatus;

    public String getIdDeliveryOrderProvider() {
        return idDeliveryOrderProvider;
    }

    public void setIdDeliveryOrderProvider(String idDeliveryOrderProvider) {
        this.idDeliveryOrderProvider = idDeliveryOrderProvider;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeliveryKey() {
        return deliveryKey;
    }

    public void setDeliveryKey(String deliveryKey) {
        this.deliveryKey = deliveryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(int deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public String getDeliveryTimeOptics() {
        return deliveryTimeOptics;
    }

    public void setDeliveryTimeOptics(String deliveryTimeOptics) {
        this.deliveryTimeOptics = deliveryTimeOptics;
    }

    public List<DeliveryOrderItemOms> getItemList() {
        return itemList;
    }

    public void setItemList(List<DeliveryOrderItemOms> itemList) {
        this.itemList = itemList;
    }

    public int getQuantityItem() {
        return quantityItem;
    }

    public void setQuantityItem(int quantityItem) {
        this.quantityItem = quantityItem;
    }

    public long getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(long deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}
