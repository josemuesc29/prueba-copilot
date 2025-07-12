package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class CustomerLifeMilesReq {
    private Long customer;
    private Long lifeMilesNumber;
    private Double invoiceValue;
    private List<OrderDetailRequest> items;
    private Integer idStoreGroup;

    public CustomerLifeMilesReq() {
    }

    public CustomerLifeMilesReq(Long customer) {
        this.customer = customer;
    }

    public CustomerLifeMilesReq(Long customer, Long lifeMilesNumber) {
        this.customer = customer;
        this.lifeMilesNumber = lifeMilesNumber;
    }

    public CustomerLifeMilesReq(Double invoiceValue, Integer idStoreGroup, List<OrderDetailRequest> items) {
        this.invoiceValue = invoiceValue;
        this.idStoreGroup = idStoreGroup;
        this.items = items;
    }

    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(Long customer) {
        this.customer = customer;
    }

    public Long getLifeMilesNumber() {
        return lifeMilesNumber;
    }

    public void setLifeMilesNumber(Long lifeMilesNumber) {
        this.lifeMilesNumber = lifeMilesNumber;
    }

    public Double getInvoiceValue() {
        return invoiceValue;
    }

    public void setInvoiceValue(Double invoiceValue) {
        this.invoiceValue = invoiceValue;
    }

    public List<OrderDetailRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderDetailRequest> items) {
        this.items = items;
    }

    public Integer getIdStoreGroup() {
        return idStoreGroup;
    }

    public void setIdStoreGroup(Integer idStoreGroup) {
        this.idStoreGroup = idStoreGroup;
    }

    public static class OrderDetailRequest{
        private Long itemId;
        private Integer quantityRequested;

        public OrderDetailRequest(Long itemId, Integer quantityRequested) {
            this.itemId = itemId;
            this.quantityRequested = quantityRequested;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public Integer getQuantityRequested() {
            return quantityRequested;
        }

        public void setQuantityRequested(Integer quantityRequested) {
            this.quantityRequested = quantityRequested;
        }
    }
}
