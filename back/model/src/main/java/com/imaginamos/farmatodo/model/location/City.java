package com.imaginamos.farmatodo.model.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.*;
import com.imaginamos.farmatodo.model.city.MunicipalityList;
import com.imaginamos.farmatodo.model.store.StoreGroup;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class City {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCity;
  @Index
  private String id;
  @Index
  private String name;
  //private String defaultStore;
  private String geoCityCode;
  private Long status;
  private String country;
  @IgnoreSave
  private List<StoreGroup> storeGroupList = new ArrayList<>();
  @IgnoreSave
  private String idCityWebSafe;
  private String phone;
  @IgnoreSave
  private DefaultStore store;
  private double latitude;
  private double longitude;
  private int defaultStore;
  @Index
  private DeliveryType deliveryType;
  private String department;
  @IgnoreSave
  @Ignore
  @IgnoreLoad
  private List<MunicipalityList> municipalList;


  public City() {
  }

  public City(String id, String name, String geoCityCode, Long status, String country, String phone, DeliveryType deliveryType) {
    this.id = id;
    this.name = name;
    this.geoCityCode = geoCityCode;
    this.status = status;
    this.country = country;
    this.phone = phone;
    this.deliveryType = deliveryType;
  }

  public String getIdCity() {
    return idCity;
  }

  public void setIdCity(String idCity) {
    this.idCity = idCity;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DefaultStore getStore() {
    return store;
  }

  public void setStore(DefaultStore store) {
    this.store = store;
  }

  public String getGeoCityCode() {
    return geoCityCode;
  }

  public void setGeoCityCode(String geoCityCode) {
    this.geoCityCode = geoCityCode;
  }

  public Long getStatus() {
    return status;
  }

  public void setStatus(Long status) {
    this.status = status;
  }

  public List<StoreGroup> getStoreGroupList() {
    return storeGroupList;
  }

  public void setStoreGroupList(List<StoreGroup> storeGroupList) {
    this.storeGroupList = storeGroupList;
  }

  public String getIdCityWebSafe() {
    return idCityWebSafe;
  }

  public void setIdCityWebSafe(String idCityWebSafe) {
    this.idCityWebSafe = idCityWebSafe;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
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

  public int getDefaultStore() {
    return defaultStore;
  }

  public void setDefaultStore(int defaultStore) {
    this.defaultStore = defaultStore;
  }

  public DeliveryType getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(DeliveryType deliveryType) {
    this.deliveryType = deliveryType;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }


  public void setMunicipalList(List<MunicipalityList> municipalList) {
    this.municipalList = municipalList;
  }

  public List<MunicipalityList> getMunicipalList() {
    return municipalList;
  }

  @Override
  public String toString() {
    return "City{" +
            "idCity='" + idCity + '\'' +
            ", id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", geoCityCode='" + geoCityCode + '\'' +
            ", status=" + status +
            ", country='" + country + '\'' +
            ", storeGroupList=" + storeGroupList +
            ", idCityWebSafe='" + idCityWebSafe + '\'' +
            ", phone='" + phone + '\'' +
            ", store=" + store +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", defaultStore=" + defaultStore +
            ", deliveryType=" + deliveryType +
            ", department='" + department + '\'' +
            '}';
  }

}
