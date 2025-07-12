package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.util.DeliveryType;

public class AddAddressRequest {
    private Long idAddress;
    private int idCustomer;
    private String nickname;
    private String city;
    private String address;
    private DeliveryType deliveryType;
    private double latitude;
    private double longitude;
    private int assignedStore;
    private String comments;
    private String courierCode;
    private String tags;
    private boolean addressWithRestriction;
    private String redZoneId;

    public AddAddressRequest() {
    }

    public AddAddressRequest(String address, String city) {
        this.address = address;
        this.city = city;
    }

    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
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

    public void setAddress(String address) {
        this.address = address;
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

    public boolean isValid(){
        return (deliveryType != null && nickname !=null && city != null && address != null);
    }

    public String getCourierCode() {return courierCode;}

    public void setCourierCode(String courierCode) { this.courierCode = courierCode; }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean getAddressWithRestriction() {
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

    @Override
    public String toString() {
        return "AddAddressRequest{" +
                "idCustomer=" + idCustomer +
                ", nickname='" + nickname + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", deliveryType=" + deliveryType +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", assignedStore=" + assignedStore +
                ", comments='" + comments + '\'' +
                '}';
    }
}
