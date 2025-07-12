package com.imaginamos.farmatodo.model.customer;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.Date;

public class AddresResponseData {
    private Long customerAddressId;
    private Long customerId;
    private Long idAddress;
    private Long idCustomer;
    private String countryId;
    private String countryName;
    private String cityId;
    private String city;
    private String cityName;
    private Long closerStoreId;
    private String closerStoreName;
    private DeliveryType deliveryType;
    private String nickname;
    private String address;
    private String comments;
    private String neighborhood;
    private String geoAddress;
    private Double longitude;
    private Double latitude;
    private Long stratum;
    private Boolean active;
    private String creationDate;
    private String source;
    private String tags;

    private boolean defaultAddress;

    private Boolean addressWithRestriction;
    private String redZoneId;

    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(Long customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Long idCustomer) {
        this.idCustomer = idCustomer;
    }

    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
        this.idAddress = idAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Long getCloserStoreId() {
        return closerStoreId;
    }

    public void setCloserStoreId(Long closerStoreId) {
        this.closerStoreId = closerStoreId;
    }

    public String getCloserStoreName() {
        return closerStoreName;
    }

    public void setCloserStoreName(String closerStoreName) {
        this.closerStoreName = closerStoreName;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getGeoAddress() {
        return geoAddress;
    }

    public void setGeoAddress(String geoAddress) {
        this.geoAddress = geoAddress;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Long getStratum() {
        return stratum;
    }

    public void setStratum(Long stratum) {
        this.stratum = stratum;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTags() {return tags;}

    public void setTags(String tags) {this.tags = tags;}

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public Boolean getAddressWithRestriction() {
        return addressWithRestriction;
    }

    public void setAddressWithRestriction(Boolean addressWithRestriction) {
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
        return new Gson().toJson(this);
    }

}
