package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class CustomerNewLoginRes {
    private Long idCustomer;
    private String email;
    private String origin;
    private String token;
    private String tokenIdWebSafe;
    private String idCustomerWebSafe;
    private List<CustomerNewLoginDataRes> list;

    public Long getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Long idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CustomerNewLoginRes{" +
                "idCustomer=" + idCustomer +
                ", email='" + email + '\'' +
                ", origin='" + origin + '\'' +
                ", token='" + token + '\'' +
                ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
                ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
                ", list=" + list +
                '}';
    }
}
