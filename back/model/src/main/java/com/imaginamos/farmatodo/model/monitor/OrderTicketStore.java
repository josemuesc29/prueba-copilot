package com.imaginamos.farmatodo.model.monitor;

public class OrderTicketStore {
    private String storeName;
    private Long ticket;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getTicket() {
        return ticket;
    }

    public void setTicket(Long ticket) {
        this.ticket = ticket;
    }
}
