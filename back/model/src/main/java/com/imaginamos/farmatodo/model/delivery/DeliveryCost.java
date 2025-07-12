package com.imaginamos.farmatodo.model.delivery;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Daniela Lozano on 2/24/17.
 * Property Imaginamos SAS
 */

@Entity
public class DeliveryCost {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idDeliveryCost;
  @Index
  private long id;
  @Index
  private String city;
  private double minAmount;
  private double maxAmount;
  private double deliveryValue;
  @IgnoreSave
  private String idDeliveryCostWebSafe;

  public String getIdDeliveryCost() {
    return idDeliveryCost;
  }

  public void setIdDeliveryCost(String idDeliveryCost) {
    this.idDeliveryCost = idDeliveryCost;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public double getMinAmount() {
    return minAmount;
  }

  public void setMinAmount(double minAmount) {
    this.minAmount = minAmount;
  }

  public double getMaxAmount() {
    return maxAmount;
  }

  public void setMaxAmount(double maxAmount) {
    this.maxAmount = maxAmount;
  }

  public double getDeliveryValue() {
    return deliveryValue;
  }

  public void setDeliveryValue(double deliveryValue) {
    this.deliveryValue = deliveryValue;
  }

  public String getIdDeliveryCostWebSafe() {
    return idDeliveryCostWebSafe;
  }

  public void setIdDeliveryCostWebSafe(String idDeliveryCostWebSafe) {
    this.idDeliveryCostWebSafe = idDeliveryCostWebSafe;
  }
}
