package com.imaginamos.farmatodo.networking.models.amplitude;

import java.util.List;
import java.util.Set;

public class EventOrderProperties {

    private String orderId;
    private String orderDeliveryType;
    private Double totalOrderPrice;
    private Integer totalOrderItems;
    private Double totalOrderDiscount;
    private String orderChannel;
    private String orderCoupon;
    private Double orderShippingCost;
    private String orderPaymentMethod;
    private Integer storeId;
    private String storeName;
    private String districtName;
    private String regionName;
    private String courierName;
    private String messengerName;
    private List<String> cclass;
    private List<String> departments;
    private List<String> divisions;
    private List<String> groups;
    private List<String> providers;
    private List<String> subclass;
    private Set<String> brandList;
    private boolean isPrime;
    private boolean selfCheckout;
    private String creditCardBin;
    private String creditCardLastNumber;
    private String gender;
    private String statusOrder;
    private String callCenterId;
    private String callCenterDoc;
    private String callCenterName;


    public String getOrderId() { return orderId; }

    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderDeliveryType() { return orderDeliveryType; }

    public void setOrderDeliveryType(String orderDeliveryType) { this.orderDeliveryType = orderDeliveryType; }

    public Double getTotalOrderPrice() { return totalOrderPrice; }

    public void setTotalOrderPrice(Double totalOrderPrice) { this.totalOrderPrice = totalOrderPrice; }

    public Integer getTotalOrderItems() { return totalOrderItems; }

    public void setTotalOrderItems(Integer totalOrderItems) { this.totalOrderItems = totalOrderItems; }

    public Double getTotalOrderDiscount() { return totalOrderDiscount; }

    public void setTotalOrderDiscount(Double totalOrderDiscount) { this.totalOrderDiscount = totalOrderDiscount; }

    public String getOrderChannel() { return orderChannel; }

    public void setOrderChannel(String orderChannel) { this.orderChannel = orderChannel; }

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

    public List<String> getCclass() { return cclass; }

    public void setCclass(List<String> cclass) { this.cclass = cclass; }

    public List<String> getDepartments() { return departments; }

    public void setDepartments(List<String> departments) { this.departments = departments; }

    public List<String> getDivisions() { return divisions; }

    public void setDivisions(List<String> divisions) { this.divisions = divisions; }

    public List<String> getGroups() { return groups; }

    public void setGroups(List<String> groups) { this.groups = groups; }

    public List<String> getProviders() { return providers; }

    public void setProviders(List<String> providers) { this.providers = providers; }

    public List<String> getSubclass() { return subclass; }

    public void setSubclass(List<String> subclass) { this.subclass = subclass; }

    public Set<String> getBrandList() {return brandList;}

    public void setBrandList(Set<String> brandList) {this.brandList = brandList;}

    public boolean getPrime() { return isPrime; }

    public void setPrime(boolean prime) { isPrime = prime; }

    public boolean isSelfCheckout() { return selfCheckout; }

    public void setSelfCheckout(boolean selfCheckout) {this.selfCheckout = selfCheckout;}

    public String getCreditCardBin() {return creditCardBin;}

    public void setCreditCardBin(String creditCardBin) { this.creditCardBin = creditCardBin; }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isPrime() { return isPrime; }

    public String getStatusOrder() { return statusOrder; }

    public void setStatusOrder(String statusOrder) { this.statusOrder = statusOrder; }

    public String getCallCenterId() { return callCenterId; }

    public void setCallCenterId(String callCenterId) { this.callCenterId = callCenterId; }

    public String getCallCenterDoc() { return callCenterDoc; }

    public void setCallCenterDoc(String callCenterDoc) { this.callCenterDoc = callCenterDoc; }

    public String getCallCenterName() { return callCenterName; }

    public void setCallCenterName(String callCenterName) { this.callCenterName = callCenterName; }

    public String getCreditCardLastNumber() { return creditCardLastNumber; }

    public void setCreditCardLastNumber(String creditCardLastNumber) { this.creditCardLastNumber = creditCardLastNumber; }
}
