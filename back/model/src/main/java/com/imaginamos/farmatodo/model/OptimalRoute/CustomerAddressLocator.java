package com.imaginamos.farmatodo.model.OptimalRoute;


public class CustomerAddressLocator {
    private Long customerAddressId;
    private String address;
    private String cityId;
    private Float latitude;
    private Float longitude;

    public CustomerAddressLocator(
            Long customerAddressId,
            String address,
            String cityId,
            Float latitude,
            Float longitude) {
        this.customerAddressId = customerAddressId;
        this.address = address;
        this.cityId = cityId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CustomerAddressLocator() {
    }

    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(Long customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }
}
