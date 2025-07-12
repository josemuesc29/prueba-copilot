package com.imaginamos.farmatodo.model.order;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class CancellationReason {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCancellationReason;
  private String description;
  private boolean status;
  private String orderNumber;
  @IgnoreSave
  private String idCancellationReasonWebSafe;

  public String getIdCancellationReason() {
    return idCancellationReason;
  }

  public void setIdCancellationReason(String idCancellationReason) {
    this.idCancellationReason = idCancellationReason;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public String getIdCancellationReasonWebSafe() {
    return idCancellationReasonWebSafe;
  }

  public void setIdCancellationReasonWebSafe(String idCancellationReasonWebSafe) {
    this.idCancellationReasonWebSafe = idCancellationReasonWebSafe;
  }
}
