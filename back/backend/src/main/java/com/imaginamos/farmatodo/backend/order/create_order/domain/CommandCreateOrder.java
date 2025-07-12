package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.imaginamos.farmatodo.model.order.DeliveryOrder;

import javax.servlet.http.HttpServletRequest;

public class CommandCreateOrder {

    private String idCustomerWebSafe;
    private String token;
    private String tokenIdWebSafe;
    private DeliveryOrder order;
    private HttpServletRequest httpServletRequest;

    public CommandCreateOrder(String idCustomerWebSafe, String token, String tokenIdWebSafe, DeliveryOrder order, HttpServletRequest httpServletRequest) {
        this.idCustomerWebSafe = idCustomerWebSafe;
        this.token = token;
        this.tokenIdWebSafe = tokenIdWebSafe;
        this.order = order;
        this.httpServletRequest = httpServletRequest;
    }

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
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

    public DeliveryOrder getOrder() {
        return order;
    }

    public void setOrder(DeliveryOrder order) {
        this.order = order;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }
}
