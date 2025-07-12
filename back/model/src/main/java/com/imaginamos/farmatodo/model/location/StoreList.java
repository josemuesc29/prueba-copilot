package com.imaginamos.farmatodo.model.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

@Entity
public class StoreList {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idStores;
  @Index
  private Long id;
  private String name;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private List<Integer> storeList;

  public String getIdStores() {
    return idStores;
  }

  public void setIdStores(String idStores) {
    this.idStores = idStores;
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

  public List<Integer> getStoreList() {
    return storeList;
  }

  public void setStoreList(List<Integer> storeList) {
    this.storeList = storeList;
  }
}


