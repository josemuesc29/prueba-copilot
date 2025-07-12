package com.imaginamos.farmatodo.networking.models.algolia;

public class CancellationReason {

    private Long id;
    private String description;
    private String messageCustomer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessageCustomer() {
        return messageCustomer;
    }

    public void setMessageCustomer(String messageCustomer) {
        this.messageCustomer = messageCustomer;
    }
}
