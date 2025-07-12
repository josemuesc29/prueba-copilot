package com.imaginamos.farmatodo.model.order;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName;
import com.imaginamos.farmatodo.model.algolia.ImageTracking;

import java.util.List;

/**
 * Created by ccrodriguez
 */

public class OrderInfoStatus {


    private int statusCode;

    private int orderId;

    private int statusId;

    private int courierId;

    private String uuid;

    private boolean isActiveSocket;

    private boolean isTransfer;

    private String httpsWebSocketUrl;

    private String httpWebSocketUrl;

    private Long customerId;

    @SerializedName("etaLongTime")
    private String ETALongTime;

    @SerializedName("etaMinutes")
    private String ETAMinutes;

    private CustomerAddressCoordinates customerAddress;

    private List<OrderStoreCoordinates> stores;

    @JsonIgnore
    private String customerGender;

    private String imageCustomerHouse;

    private String imageStore;

    private String imageMotorbikeDelivery;

    private String messengerId;

    private String messengerName;

    private String messengerPhone;

    private String messengerPhoto;

    private String urlTracking;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getCourierId() {
        return courierId;
    }

    public void setCourierId(int courierId) {
        this.courierId = courierId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isActiveSocket() {
        return isActiveSocket;
    }

    public void setActiveSocket(boolean activeSocket) {
        isActiveSocket = activeSocket;
    }

    public boolean isTransfer() { return isTransfer; }

    public void setTransfer(boolean transfer) { isTransfer = transfer; }

    public String getHttpsWebSocketUrl() { return httpsWebSocketUrl; }

    public void setHttpsWebSocketUrl(String httpsWebSocketUrl) { this.httpsWebSocketUrl = httpsWebSocketUrl; }

    public String getHttpWebSocketUrl() { return httpWebSocketUrl; }

    public void setHttpWebSocketUrl(String httpWebSocketUrl) { this.httpWebSocketUrl = httpWebSocketUrl; }

    public String getETALongTime() {
        return ETALongTime;
    }

    public void setETALongTime(String ETALongTime) {
        this.ETALongTime = ETALongTime;
    }

    public String getETAMinutes() {
        return ETAMinutes;
    }

    public void setETAMinutes(String ETAMinutes) {
        this.ETAMinutes = ETAMinutes;
    }

    public CustomerAddressCoordinates getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(CustomerAddressCoordinates customerAddress) {
        this.customerAddress = customerAddress;
    }

    public List<OrderStoreCoordinates> getStores() {
        return stores;
    }

    public void setStores(List<OrderStoreCoordinates> stores) {
        this.stores = stores;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerGender() {
        return customerGender;
    }

    public void setCustomerGender(String customerGender) {
        this.customerGender = customerGender;
    }

    public String getImageCustomerHouse() {
        return imageCustomerHouse;
    }

    public void setImageCustomerHouse(String imageCustomerHouse) {
        this.imageCustomerHouse = imageCustomerHouse;
    }

    public String getImageStore() {
        return imageStore;
    }

    public void setImageStore(String imageStore) {
        this.imageStore = imageStore;
    }

    public String getImageMotorbikeDelivery() {
        return imageMotorbikeDelivery;
    }

    public void setImageMotorbikeDelivery(String imageMotorbikeDelivery) {
        this.imageMotorbikeDelivery = imageMotorbikeDelivery;
    }

    public String getMessengerId() { return messengerId; }

    public void setMessengerId(String messengerId) { this.messengerId = messengerId; }

    public String getMessengerName() { return messengerName; }

    public void setMessengerName(String messengerName) { this.messengerName = messengerName; }

    public String getMessengerPhone() { return messengerPhone; }

    public void setMessengerPhone(String messengerPhone) { this.messengerPhone = messengerPhone; }

    public String getMessengerPhoto() { return messengerPhoto; }

    public void setMessengerPhoto(String messengerPhoto) { this.messengerPhoto = messengerPhoto; }

    public String getUrlTracking() { return urlTracking; }

    public void setUrlTracking(String urlTracking) { this.urlTracking = urlTracking; }

    @Override
    public String toString() {
        return "OrderInfoStatus{" +
                "statusCode=" + statusCode +
                ", orderId=" + orderId +
                ", statusId=" + statusId +
                ", customerId=" + customerId +
                ", courierId=" + courierId +
                ", uuid='" + uuid + '\'' +
                ", isActiveSocket=" + isActiveSocket +
                ", isTransfer=" + isTransfer +
                ", httpsWebSocketUrl='" + httpsWebSocketUrl + '\'' +
                ", httpWebSocketUrl='" + httpWebSocketUrl + '\'' +
                ", ETALongTime= '" + ETALongTime + '\'' +
                ", ETAMinutes= '" + ETAMinutes + '\'' +
                ", messengerId=" + messengerId +
                ", messengerName=" + messengerName +
                ", messengerPhone=" + messengerPhone +
                ", messengerPhoto=" + messengerPhoto +
                ", urlTracking=" + urlTracking +
                '}';
    }


}
