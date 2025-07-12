package com.imaginamos.farmatodo.model.order;

public class GetOrderSummaryData {
    private Long orderId;
    private Double orderValue;
    private Double deliveryValue;
    private Double discountValue;
    private Double invoiceValue;
    private Double providerDeliveryValue;

    public GetOrderSummaryData(Long orderId, Double orderValue, Double deliveryValue, Double discountValue, Double invoiceValue, Double providerDeliveryValue) {
        this.orderId = orderId;
        this.orderValue = orderValue;
        this.deliveryValue = deliveryValue;
        this.discountValue = discountValue;
        this.invoiceValue = invoiceValue;
        this.providerDeliveryValue = providerDeliveryValue;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public Double getDeliveryValue() {
        return deliveryValue;
    }

    public void setDeliveryValue(Double deliveryValue) {
        this.deliveryValue = deliveryValue;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getInvoiceValue() {
        return invoiceValue;
    }

    public void setInvoiceValue(Double invoiceValue) {
        this.invoiceValue = invoiceValue;
    }

    public Double getProviderDeliveryValue() {
        return providerDeliveryValue;
    }

    public void setProviderDeliveryValue(Double providerDeliveryValue) {
        this.providerDeliveryValue = providerDeliveryValue;
    }

    @Override
    public String toString() {
        return "GetOrderSummaryData{" +
                "orderId=" + orderId +
                ", orderValue=" + orderValue +
                ", deliveryValue=" + deliveryValue +
                ", discountValue=" + discountValue +
                ", invoiceValue=" + invoiceValue +
                ", providerDeliveryValue=" + providerDeliveryValue +
                '}';
    }
}
