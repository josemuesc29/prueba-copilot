package com.imaginamos.farmatodo.model.customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerNewLoginListRes {

    private List<CustomerDataLoginRes> customers;

    public CustomerNewLoginListRes() {
    }

    public CustomerNewLoginListRes(List<CustomerDataLoginRes> customers) {
        this.customers = customers;
    }

    public CustomerNewLoginListRes(CustomerDataLoginRes customer) {
        this.customers = new ArrayList<>();
        this.customers.add(customer);
    }

    public List<CustomerDataLoginRes> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerDataLoginRes> customers) {
        this.customers = customers;
    }
}
