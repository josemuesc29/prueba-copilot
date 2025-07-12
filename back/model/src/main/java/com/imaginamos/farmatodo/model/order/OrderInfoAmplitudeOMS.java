package com.imaginamos.farmatodo.model.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.ArrayList;

public class OrderInfoAmplitudeOMS {

    public String cityCode;
    public String email;
    public String firstName;
    public String lastName;
    public String phone;
    public String country;
    public String orderId;
    public String orderDeliveryType;
    public double totalOrderPrice;
    public int totalOrderItems;
    public double totalOrderDiscount;
    public String orderCoupon;
    public double orderShippingCost;
    public String orderPaymentMethod;
    public int storeId;
    public String storeName;
    public String createDate;
    public String districtName;
    public String regionName;
    public String courierName;
    public String messengerName;
    public String source;
    public boolean selfCheckout;
    public String creditCardBin;
    private String creditCardLastNumber;
    public int paymentMethodId;
    public String customerReview;
    public String gender;
    public Double customerId;
    public ArrayList<OrderInfoItemsAmplitudeOMS> items;
    private String callCenterId;
    private String callCenterDoc;
    private String callCenterName;

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOrderId() { return orderId; }

    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderDeliveryType() { return orderDeliveryType; }

    public void setOrderDeliveryType(String orderDeliveryType) { this.orderDeliveryType = orderDeliveryType; }

    public Double getTotalOrderPrice() { return totalOrderPrice; }

    public void setTotalOrderPrice(Double totalOrderPrice) { this.totalOrderPrice = totalOrderPrice; }

    public Integer getTotalOrderItems() { return totalOrderItems; }

    public void setTotalOrderItems(Integer totalOrderItems) { this.totalOrderItems = totalOrderItems;}

    public Double getTotalOrderDiscount() { return totalOrderDiscount;}

    public void setTotalOrderDiscount(Double totalOrderDiscount) { this.totalOrderDiscount = totalOrderDiscount; }

    public String getOrderCoupon() { return orderCoupon; }

    public void setOrderCoupon(String orderCoupon) { this.orderCoupon = orderCoupon; }

    public Double getOrderShippingCost() { return orderShippingCost; }

    public void setOrderShippingCost(Double orderShippingCost) { this.orderShippingCost = orderShippingCost; }

    public String getOrderPaymentMethod() { return orderPaymentMethod; }

    public void setOrderPaymentMethod(String orderPaymentMethod) { this.orderPaymentMethod = orderPaymentMethod; }

    public Integer getStoreId() { return storeId; }

    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public String getStoreName() {return storeName;}

    public void setStoreName(String storeName) {this.storeName = storeName;}

    public String getCreateDate() { return createDate; }

    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getDistrictName() { return districtName; }

    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getRegionName() { return regionName; }

    public void setRegionName(String regionName) { this.regionName = regionName; }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getMessengerName() {
        return messengerName;
    }

    public void setMessengerName(String messengerName) {
        this.messengerName = messengerName;
    }

    public String getSource() { return source; }

    public void setSource(String source) { this.source = source; }

    public boolean isSelfCheckout() { return selfCheckout; }

    public void setSelfCheckout(boolean selfCheckout) { this.selfCheckout = selfCheckout; }

    public String getCreditCardBin() { return creditCardBin; }

    public void setCreditCardBin(String creditCardBin) { this.creditCardBin = creditCardBin; }

    public Integer getPaymentMethodId() { return paymentMethodId; }

    public void setPaymentMethodId(Integer paymentMethodId) { this.paymentMethodId = paymentMethodId;  }

    public String getCustomerReview() {
        return customerReview;
    }

    public void setCustomerReview(String customerReview) {
        this.customerReview = customerReview;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setTotalOrderPrice(double totalOrderPrice) { this.totalOrderPrice = totalOrderPrice; }

    public void setTotalOrderItems(int totalOrderItems) { this.totalOrderItems = totalOrderItems; }

    public void setTotalOrderDiscount(double totalOrderDiscount) { this.totalOrderDiscount = totalOrderDiscount; }

    public void setOrderShippingCost(double orderShippingCost) { this.orderShippingCost = orderShippingCost; }

    public void setStoreId(int storeId) { this.storeId = storeId; }

    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId;}

    public ArrayList<OrderInfoItemsAmplitudeOMS> getItems() { return items; }

    public void setItems(ArrayList<OrderInfoItemsAmplitudeOMS> items) { this.items = items; }

    public Double getCustomerId() { return customerId; }

    public void setCustomerId(Double customerId) { this.customerId = customerId; }

    public String getCallCenterId() { return callCenterId; }

    public void setCallCenterId(String callCenterId) { this.callCenterId = callCenterId; }

    public String getCallCenterDoc() { return callCenterDoc; }

    public void setCallCenterDoc(String callCenterDoc) { this.callCenterDoc = callCenterDoc; }

    public String getCallCenterName() { return callCenterName; }

    public void setCallCenterName(String callCenterName) { this.callCenterName = callCenterName; }

    public String getCreditCardLastNumber() { return creditCardLastNumber; }

    public void setCreditCardLastNumber(String creditCardLastNumber) { this.creditCardLastNumber = creditCardLastNumber; }

    public String toStringJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
