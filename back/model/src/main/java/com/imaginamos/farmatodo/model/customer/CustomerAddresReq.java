package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.util.DeliveryType;

public class CustomerAddresReq {
    private Long customerId;
    private Long customerAddressId;
    private String nickname;
    private String cityId;
    private String address;
    private DeliveryType deliveryType;
    private Double latitude;
    private Double longitude;
    private int closerStoreId;
    private String comments;
    private String tags;
    private boolean addressWithRestriction;
    private String redZoneId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(Long customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getCloserStoreId() {
        return closerStoreId;
    }

    public void setCloserStoreId(int closerStoreId) {
        this.closerStoreId = closerStoreId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTags() {return tags;}

    public void setTags(String tags) {this.tags = tags;}

    public boolean isAddressWithRestriction() {
        return addressWithRestriction;
    }

    public void setAddressWithRestriction(boolean addressWithRestriction) {
        this.addressWithRestriction = addressWithRestriction;
    }

    public String getRedZoneId() {
        return redZoneId;
    }

    public void setRedZoneId(String redZoneId) {
        this.redZoneId = redZoneId;
    }
}
