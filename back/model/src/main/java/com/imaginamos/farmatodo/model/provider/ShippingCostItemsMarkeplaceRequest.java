package com.imaginamos.farmatodo.model.provider;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

import java.util.List;

public class ShippingCostItemsMarkeplaceRequest {

    private List<ItemAlgolia> itemList;
    private String daneCodeCustomer;
    private  String  addressCustomer;

    public ShippingCostItemsMarkeplaceRequest() {
    }

    public List<ItemAlgolia> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemAlgolia> itemList) {
        this.itemList = itemList;
    }

    public String getDaneCodeCustomer() {
        return daneCodeCustomer;
    }

    public void setDaneCodeCustomer(String daneCodeCustomer) {
        this.daneCodeCustomer = daneCodeCustomer;
    }

    public String getAddressCustomer() {
        return addressCustomer;
    }

    public void setAddressCustomer(String addressCustomer) {
        this.addressCustomer = addressCustomer;
    }


}