package com.imaginamos.farmatodo.model.order;


import java.util.List;

public class GetCoodinatesCustomerAndAddressByOrderResponseData {

    private CustomerAddressCoordinates customerAddress;
    private List<OrderStoreCoordinates> stores;

    public CustomerAddressCoordinates getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(CustomerAddressCoordinates customerAddress) {
        this.customerAddress = customerAddress;
    }

    public List<OrderStoreCoordinates> getStores() {
        return stores;
    }

    public void setStores(List<OrderStoreCoordinates> stores) {
        this.stores = stores;
    }
}
