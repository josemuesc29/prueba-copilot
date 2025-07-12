package com.imaginamos.farmatodo.model.store;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;
import com.imaginamos.farmatodo.model.location.Net;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.List;

/**
 * Created by mileniopc on 11/24/16.
 * Property of Imaginamos.
 */

@Entity
public class Store {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idStore;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<StoreGroup> owner;
  @Index
  private Long id;
  @Index
  private String name;
  private String address;
  private double latitude;
  private double longitude;
  @IgnoreSave
  private String city;
  private Long status;
  private String operationStartTime;
  private String operationEndTime;
  private String photo;
  private String phone;
  private String scheduleMsn;
  @IgnoreSave
  private List<Net> nets;
  @IgnoreSave
  private String idStoreWebSafe;
  private DeliveryType deliveryType;

  public String getIdStore() {
    return idStore;
  }

  public void setIdStore(String idStore) {
    this.idStore = idStore;
  }

  public Ref<StoreGroup> getOwner() {
    return owner;
  }

  public void setOwner(Ref<StoreGroup> owner) {
    this.owner = owner;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Long getStatus() {
    return status;
  }

  public void setStatus(Long status) {
    this.status = status;
  }

  public String getOperationStartTime() {
    return operationStartTime;
  }

  public void setOperationStartTime(String operationStartTime) {
    this.operationStartTime = operationStartTime;
  }

  public String getOperationEndTime() {
    return operationEndTime;
  }

  public void setOperationEndTime(String operationEndTime) {
    this.operationEndTime = operationEndTime;
  }

  public String getPhoto() {
    return photo;
  }

  public void setPhoto(String photo) {
    this.photo = photo;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getScheduleMsn() {
    return scheduleMsn;
  }

  public void setScheduleMsn(String scheduleMsn) {
    this.scheduleMsn = scheduleMsn;
  }

  public List<Net> getNets() {
    return nets;
  }

  public void setNets(List<Net> nets) {
    this.nets = nets;
  }

  public String getIdStoreWebSafe() {
    return idStoreWebSafe;
  }

  public void setIdStoreWebSafe(String idStoreWebSafe) {
    this.idStoreWebSafe = idStoreWebSafe;
  }

  public DeliveryType getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(DeliveryType deliveryType) {
    this.deliveryType = deliveryType;
  }

  @Override
  public String toString() {
    return "Store{" +
            "idStore='" + idStore + '\'' +
            ", owner=" + owner +
            ", id=" + id +
            ", name='" + name + '\'' +
            ", address='" + address + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", city='" + city + '\'' +
            ", status=" + status +
            ", operationStartTime='" + operationStartTime + '\'' +
            ", operationEndTime='" + operationEndTime + '\'' +
            ", photo='" + photo + '\'' +
            ", phone='" + phone + '\'' +
            ", scheduleMsn='" + scheduleMsn + '\'' +
            ", nets=" + nets +
            ", idStoreWebSafe='" + idStoreWebSafe + '\'' +
            ", deliveryType=" + deliveryType +
            '}';
  }
}
