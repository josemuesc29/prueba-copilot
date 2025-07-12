package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.util.DeliveryType;

public class UpdateAddressRequest {
    public int idAddress;
    public int idCustomer;
    public String nickname;
    public String city;
    public String address;
    public double latitude;
    public double longitude;
    public int assignedStore;
    public String comments;
    public DeliveryType deliveryType;
    private String tags;
    private boolean addressWithRestriction;
    private String redZoneId;

    public UpdateAddressRequest() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getAssignedStore() {
        return assignedStore;
    }

    public void setAssignedStore(int assignedStore) {
        this.assignedStore = assignedStore;
    }

    public int getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(int idAddress) {
        this.idAddress = idAddress;
    }

    public int getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(int idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTags() {return tags;}

    public void setTags(String tags) {this.tags = tags;    }

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

    public boolean isValid(){
        return (idAddress > 0 && nickname !=null && city != null && address != null  && assignedStore > 0);
    }
}
