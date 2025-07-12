package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class CustomerDataLoginRes {
    private Long idCustomer;
    private String token;
    private String tokenIdWebSafe;
    private String idCustomerWebSafe;
    private List<CustomerNewLoginDataRes> list;

    public CustomerDataLoginRes() {
    }

    public CustomerDataLoginRes(CustomerNewLoginRes customer) {
        this.idCustomer = customer.getIdCustomer();
        this.token = customer.getToken();
        this.tokenIdWebSafe = customer.getTokenIdWebSafe();
        this.idCustomerWebSafe = customer.getIdCustomerWebSafe();
        this.list = customer.getList();
    }

    public Long getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Long idCustomer) {
        this.idCustomer = idCustomer;
    }

    public List<CustomerNewLoginDataRes> getList() {
        return list;
    }

    public void setList(List<CustomerNewLoginDataRes> list) {
        this.list = list;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
    }

    public void clearSecurityData() {
        this.setIdCustomerWebSafe(null);
        this.setToken(null);
        this.setTokenIdWebSafe(null);
    }

    @Override
    public String toString() {
        return "CustomerNewLoginRes{" +
                "idCustomer=" + idCustomer +
                ", token='" + token + '\'' +
                ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
                ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
                ", list=" + list +
                '}';
    }
}
