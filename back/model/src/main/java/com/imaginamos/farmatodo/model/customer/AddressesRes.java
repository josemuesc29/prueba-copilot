package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.List;

public class AddressesRes {

    private List<Address> data;

    private List<Address> addresses;

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Address> getData() { return data; }

    public void setData(List<Address> data) { this.data = data; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}