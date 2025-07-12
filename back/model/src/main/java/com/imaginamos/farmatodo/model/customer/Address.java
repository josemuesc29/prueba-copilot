package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.Objects;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class Address {

  private Integer idCustomer;
  private Integer idAddress;
  private String city;
  private String address;
  private String address2;
  private double latitude;
  private double longitude;
  private String comments;
  private String courierCode;
  private String nickname;
  private Boolean newAddress;
  private int closerStoreId;
  private DeliveryType deliveryType;
  private Integer defaultStore;
  boolean active;
  private String countryName;
  private String cityName;
  private String tags;

  private boolean defaultAddress;
  private String redZoneId;

  public Address() {
  }

  public Address(AddresResponseData data) {
    if (Objects.nonNull(data.getCustomerId())) {
      this.idCustomer = data.getCustomerId().intValue();
    }else if (Objects.nonNull(data.getIdCustomer())){
      this.idCustomer = data.getIdCustomer().intValue();
    }else {
      this.idCustomer = 0;
    }

    if (Objects.nonNull(data.getCustomerAddressId())) {
      this.idAddress = data.getCustomerAddressId().intValue();
    }else if (Objects.nonNull(data.getIdAddress())){
      this.idAddress  = data.getIdAddress().intValue();
    }else {
      this.idAddress  = 0;
    }

    if (Objects.nonNull(data.getCityId())) {
      this.city = data.getCityId();
    }else if (Objects.nonNull(data.getCity())){
      this.city = data.getCity();
    }

    this.address = data.getAddress();

    if (Objects.nonNull(data.getLatitude())){
      this.latitude = data.getLatitude();
    }else {
      this.latitude = 0D;
    }

    if(Objects.nonNull(data.getLongitude())) {
      this.longitude = data.getLongitude();
    }else {
      this.longitude = 0D;
    }

    this.comments = data.getComments();
    this.nickname = data.getNickname();
    this.closerStoreId = data.getCloserStoreId().intValue();
    this.deliveryType = data.getDeliveryType();
    this.active = data.getActive();
    this.cityName = data.getCityName();
    this.tags = data.getTags();
    this.defaultAddress = data.isDefaultAddress();
    this.redZoneId = data.getRedZoneId();
  }

  public Integer getIdCustomer() {
    return idCustomer;
  }

  public void setIdCustomer(Integer idCustomer) {
    this.idCustomer = idCustomer;
  }

  public Integer getIdAddress() {
    return idAddress;
  }

  public void setIdAddress(Integer idAddress) {
    this.idAddress = idAddress;
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

  public String getAddress2() {
    return address2;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
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

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getCourierCode() {
    return courierCode;
  }

  public void setCourierCode(String courierCode) {
    this.courierCode = courierCode;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public Boolean getNewAddress() {
    return newAddress;
  }

  public void setNewAddress(Boolean newAddress) {
    this.newAddress = newAddress;
  }

  public int getCloserStoreId() {
    return closerStoreId;
  }

  public void setCloserStoreId(int closerStoreId) {
    this.closerStoreId = closerStoreId;
  }

  public DeliveryType getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(DeliveryType deliveryType) {
    this.deliveryType = deliveryType;
  }

  public Integer getDefaultStore() {
    return defaultStore;
  }

  public void setDefaultStore(Integer defaultStore) {
    this.defaultStore = defaultStore;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public String getTags() {return tags;}

  public void setTags(String tags) {this.tags = tags;}

  public boolean isDefaultAddress() {
    return defaultAddress;
  }

  public void setDefaultAddress(boolean defaultAddress) {
    this.defaultAddress = defaultAddress;
  }


  public String getRedZoneId() {
    return redZoneId;
  }

  public void setRedZoneId(String redZoneId) {
    this.redZoneId = redZoneId;
  }

  @Override
  public String toString() {
    return "Address{" +
            "idCustomer=" + idCustomer +
            ", idAddress=" + idAddress +
            ", city='" + city + '\'' +
            ", address='" + address + '\'' +
            ", address2='" + address2 + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", comments='" + comments + '\'' +
            ", courierCode='" + courierCode + '\'' +
            ", nickname='" + nickname + '\'' +
            ", newAddress=" + newAddress +
            ", closerStoreId=" + closerStoreId +
            ", deliveryType=" + deliveryType +
            ", defaultStore=" + defaultStore +
            ", active=" + active +
            ", countryName='" + countryName + '\'' +
            ", cityName='" + cityName + '\'' +
            ", tags='" + tags + '\'' +
            '}';
  }
}
