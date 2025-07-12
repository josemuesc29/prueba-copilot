package com.imaginamos.farmatodo.model.monitor;

import java.util.List;

public class OrderBillResponse {
    private String code;
    private List<OrderTicketStore> orderTicketStores;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<OrderTicketStore> getOrderTicketStores() {
        return orderTicketStores;
    }

    public void setOrderTicketStores(List<OrderTicketStore> orderTicketStores) {
        this.orderTicketStores = orderTicketStores;
    }
}
