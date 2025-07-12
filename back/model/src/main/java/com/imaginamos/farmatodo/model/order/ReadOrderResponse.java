package com.imaginamos.farmatodo.model.order;

import com.imaginamos.farmatodo.model.product.ItemOrder;

import java.util.Date;
import java.util.List;

public class ReadOrderResponse {
    private String id;
    private Long createDate;
    private String deliveryType;
    private String customer;
    private Long customerDocumentNumber;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String city;
    private String cityName;
    private String addressId;
    private String address;
    private String addressDetails;
    private Double addressLongitude;
    private Double addressLatitude;
    private Long paymentMethod;
    private String paymentMethodName;
    private String paymentCardId;
    private Integer quotas;
    private String coupon;
    private String comments;
    private Integer totalQuantity;
    private Double subtotalValue;
    private Double deliveryValue;
    private Double discountValue;
    private Double totalValue;
    private String cancellationReasonId;
    private String cancellationReasonDescription;
    private String cancellationComments;
    private String status;
    private String securityToken;
    private String source;
    private Long store;
    private String orderDetails;
    private Long pickingDate;
    private Double weight;
    private Double orderValue;
    private Double invoiceValue;
    private Long customerReview;
    private String customerReviewComments;
    private List<ItemOrder> items;
    private List<Tracing> tracing;
    private double providerDeliveryValue;

    public ReadOrderResponse() {
    }

    public ReadOrderResponse(String id, Long createDate, String deliveryType, String customer, Long customerDocumentNumber, String customerName, String customerPhone, String customerEmail, String city, String cityName, String addressId, String address, String addressDetails, Double addressLongitude, Double addressLatitude, Long paymentMethod, String paymentMethodName, String paymentCardId, Integer quotas, String coupon, String comments, Integer totalQuantity, Double subtotalValue, Double deliveryValue, Double discountValue, Double totalValue, String cancellationReasonId, String cancellationReasonDescription, String cancellationComments, String status, String securityToken, String source, Long store, String orderDetails, Long pickingDate, Double weight, Double orderValue, Double invoiceValue, Long customerReview, String customerReviewComments, List<ItemOrder> items, List<Tracing> tracing, double providerDeliveryValue) {
        this.id = id;
        this.createDate = createDate;
        this.deliveryType = deliveryType;
        this.customer = customer;
        this.customerDocumentNumber = customerDocumentNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.city = city;
        this.cityName = cityName;
        this.addressId = addressId;
        this.address = address;
        this.addressDetails = addressDetails;
        this.addressLongitude = addressLongitude;
        this.addressLatitude = addressLatitude;
        this.paymentMethod = paymentMethod;
        this.paymentMethodName = paymentMethodName;
        this.paymentCardId = paymentCardId;
        this.quotas = quotas;
        this.coupon = coupon;
        this.comments = comments;
        this.totalQuantity = totalQuantity;
        this.subtotalValue = subtotalValue;
        this.deliveryValue = deliveryValue;
        this.discountValue = discountValue;
        this.totalValue = totalValue;
        this.cancellationReasonId = cancellationReasonId;
        this.cancellationReasonDescription = cancellationReasonDescription;
        this.cancellationComments = cancellationComments;
        this.status = status;
        this.securityToken = securityToken;
        this.source = source;
        this.store = store;
        this.orderDetails = orderDetails;
        this.pickingDate = pickingDate;
        this.weight = weight;
        this.orderValue = orderValue;
        this.invoiceValue = invoiceValue;
        this.customerReview = customerReview;
        this.customerReviewComments = customerReviewComments;
        this.items = items;
        this.tracing = tracing;
        this.providerDeliveryValue = providerDeliveryValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Long getCustomerDocumentNumber() {
        return customerDocumentNumber;
    }

    public void setCustomerDocumentNumber(Long customerDocumentNumber) {
        this.customerDocumentNumber = customerDocumentNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }

    public Double getAddressLongitude() {
        return addressLongitude;
    }

    public void setAddressLongitude(Double addressLongitude) {
        this.addressLongitude = addressLongitude;
    }

    public Double getAddressLatitude() {
        return addressLatitude;
    }

    public void setAddressLatitude(Double addressLatitude) {
        this.addressLatitude = addressLatitude;
    }

    public Long getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Long paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public String getPaymentCardId() {
        return paymentCardId;
    }

    public void setPaymentCardId(String paymentCardId) {
        this.paymentCardId = paymentCardId;
    }

    public Integer getQuotas() {
        return quotas;
    }

    public void setQuotas(Integer quotas) {
        this.quotas = quotas;
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Double getSubtotalValue() {
        return subtotalValue;
    }

    public void setSubtotalValue(Double subtotalValue) {
        this.subtotalValue = subtotalValue;
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

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public String getCancellationReasonId() {
        return cancellationReasonId;
    }

    public void setCancellationReasonId(String cancellationReasonId) {
        this.cancellationReasonId = cancellationReasonId;
    }

    public String getCancellationReasonDescription() {
        return cancellationReasonDescription;
    }

    public void setCancellationReasonDescription(String cancellationReasonDescription) {
        this.cancellationReasonDescription = cancellationReasonDescription;
    }

    public String getCancellationComments() {
        return cancellationComments;
    }

    public void setCancellationComments(String cancellationComments) {
        this.cancellationComments = cancellationComments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getStore() {
        return store;
    }

    public void setStore(Long store) {
        this.store = store;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Long getPickingDate() {
        return pickingDate;
    }

    public void setPickingDate(Long pickingDate) {
        this.pickingDate = pickingDate;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public Double getInvoiceValue() {
        return invoiceValue;
    }

    public void setInvoiceValue(Double invoiceValue) {
        this.invoiceValue = invoiceValue;
    }

    public Long getCustomerReview() {
        return customerReview;
    }

    public void setCustomerReview(Long customerReview) {
        this.customerReview = customerReview;
    }

    public String getCustomerReviewComments() {
        return customerReviewComments;
    }

    public void setCustomerReviewComments(String customerReviewComments) {
        this.customerReviewComments = customerReviewComments;
    }

    public List<ItemOrder> getItems() {
        return items;
    }

    public void setItems(List<ItemOrder> items) {
        this.items = items;
    }

    public List<Tracing> getTracing() {
        return tracing;
    }

    public void setTracing(List<Tracing> tracing) {
        this.tracing = tracing;
    }

    public double getProviderDeliveryValue() {
        return providerDeliveryValue;
    }

    public void setProviderDeliveryValue(double providerDeliveryValue) {
        this.providerDeliveryValue = providerDeliveryValue;
    }
}
