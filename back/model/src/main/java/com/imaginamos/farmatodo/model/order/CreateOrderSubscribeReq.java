package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class CreateOrderSubscribeReq {
    private float discountRate;
    private int quotas;
    private String customerAddressDetails;
    private String deliveryType;
    private String source;
    private String storeId;
    private String storeSelectMode;
    private int customerAddressId;
    private String orderDetails;
    private List<Object> coupons = null;
    private int paymentMethodId;
    private int customerId;
    private List<Item> items = null;
    private List<Object> providers = null;

    public class Item {

        private int itemId;
        private int quantityRequested;

        public Item() {

        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public int getQuantityRequested() {
            return quantityRequested;
        }

        public void setQuantityRequested(int quantityRequested) {
            this.quantityRequested = quantityRequested;
        }
    }


    public CreateOrderSubscribeReq() {

    }

    public float getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(float discountRate) {
        this.discountRate = discountRate;
    }

    public int getQuotas() {
        return quotas;
    }

    public void setQuotas(int quotas) {
        this.quotas = quotas;
    }

    public String getCustomerAddressDetails() {
        return customerAddressDetails;
    }

    public void setCustomerAddressDetails(String customerAddressDetails) {
        this.customerAddressDetails = customerAddressDetails;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreSelectMode() {
        return storeSelectMode;
    }

    public void setStoreSelectMode(String storeSelectMode) {
        this.storeSelectMode = storeSelectMode;
    }

    public int getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(int customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public List<Object> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<Object> coupons) {
        this.coupons = coupons;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Object> getProviders() {
        return providers;
    }

    public void setProviders(List<Object> providers) {
        this.providers = providers;
    }
}
