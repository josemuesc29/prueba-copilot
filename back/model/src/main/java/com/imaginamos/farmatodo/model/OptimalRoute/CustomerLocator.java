package com.imaginamos.farmatodo.model.OptimalRoute;

public class CustomerLocator {
    private Long customerId;
    private CustomerAddressLocator customerAddress;

    public CustomerLocator(Long customerId, CustomerAddressLocator customerAddress) {
        this.customerId = customerId;
        this.customerAddress = customerAddress;
    }

    public CustomerLocator() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public CustomerAddressLocator getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(CustomerAddressLocator customerAddress) {
        this.customerAddress = customerAddress;
    }
}
