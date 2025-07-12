package com.imaginamos.farmatodo.model.product;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.store.Store;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class ProductInStore {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idStore;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<Item> idItem;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Store> storeInformationKey;
  private int stock;
  private double price;
  private double offerPrice;
  @IgnoreSave
  private String idStoreWebSafe;

  public String getIdStore() {
    return idStore;
  }

  public void setIdStore(String idStore) {
    this.idStore = idStore;
  }

  public Ref<Item> getIdItem() {
    return idItem;
  }

  public void setIdItem(Ref<Item> idItem) {
    this.idItem = idItem;
  }

  public Key<Store> getStoreInformationKey() {
    return storeInformationKey;
  }

  public void setStoreInformationKey(Key<Store> storeInformationKey) {
    this.storeInformationKey = storeInformationKey;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public double getOfferPrice() {
    return offerPrice;
  }

  public void setOfferPrice(double offerPrice) {
    this.offerPrice = offerPrice;
  }

  public String getIdStoreWebSafe() {
    return idStoreWebSafe;
  }

  public void setIdStoreWebSafe(String idStoreWebSafe) {
    this.idStoreWebSafe = idStoreWebSafe;
  }
}
