package com.imaginamos.farmatodo.model.store;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;
import com.imaginamos.farmatodo.model.location.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mileniopc on 11/24/16.
 * Property of Imaginamos.
 */

@Entity
public class StoreGroup {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idStoreGroup;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<City> owner;
  @Index
  private Long storeGroupId;
  @Index
  private String storeGroupName;
  @IgnoreSave
  private List<Store> storeList = new ArrayList<>();
  @IgnoreSave
  private String idStoreGroupWebSafe;

  public String getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(String idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
  }

  public Ref<City> getOwner() {
    return owner;
  }

  public void setOwner(Ref<City> owner) {
    this.owner = owner;
  }

  public Long getStoreGroupId() {
    return storeGroupId;
  }

  public void setStoreGroupId(Long storeGroupId) {
    this.storeGroupId = storeGroupId;
  }

  public String getStoreGroupName() {
    return storeGroupName;
  }

  public void setStoreGroupName(String storeGroupName) {
    this.storeGroupName = storeGroupName;
  }

  public List<Store> getStoreList() {
    return storeList;
  }

  public void setStoreList(List<Store> storeList) {
    this.storeList = storeList;
  }

  public String getIdStoreGroupWebSafe() {
    return idStoreGroupWebSafe;
  }

  public void setIdStoreGroupWebSafe(String idStoreGroupWebSafe) {
    this.idStoreGroupWebSafe = idStoreGroupWebSafe;
  }
}
