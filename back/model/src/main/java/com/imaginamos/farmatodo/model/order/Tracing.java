package com.imaginamos.farmatodo.model.order;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

/**
 * Created by Eric on 15/03/2017.
 */

@Entity
public class Tracing {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idTracing;
  @Index
  private long id;
  private long createDate;
  private String comments;
  private Long cancellationReason;
  private Long courier;
  private int status;
  private int minutesToDeliver;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<DeliveryOrder> deliveryOrderId;
  @IgnoreSave
  private Long deliveryStatus;
  @IgnoreSave
  private Long providerId;
  @IgnoreSave
  private String statusCode;
  @IgnoreSave
  private String uuid;


  public String getIdTracing() {
    return idTracing;
  }

  public void setIdTracing(String idTracing) {
    this.idTracing = idTracing;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getCreateDate() {
    return createDate;
  }

  public void setCreateDate(long createDate) {
    this.createDate = createDate;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getMinutesToDeliver() {
    return minutesToDeliver;
  }

  public void setMinutesToDeliver(int minutesToDeliver) {
    this.minutesToDeliver = minutesToDeliver;
  }

  public Ref<DeliveryOrder> getDeliveryOrderId() {
    return deliveryOrderId;
  }

  public void setDeliveryOrderId(Ref<DeliveryOrder> deliveryOrderId) {
    this.deliveryOrderId = deliveryOrderId;
  }

  public Long getCancellationReason() {
    return cancellationReason;
  }

  public void setCancellationReason(Long cancellationReason) {
    this.cancellationReason = cancellationReason;
  }

  public Long getCourier() {
    return courier;
  }

  public void setCourier(Long courier) {
    this.courier = courier;
  }

  public Long getDeliveryStatus() { return deliveryStatus; }

  public void setDeliveryStatus(Long deliveryStatus) { this.deliveryStatus = deliveryStatus; }

  public Long getProviderId() { return providerId; }

  public void setProviderId(Long providerId) { this.providerId = providerId; }

  public String getStatusCode() { return statusCode; }

  public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

  public String getUuid() { return uuid; }

  public void setUuid(String uuid) { this.uuid = uuid; }

  @Override
  public String toString() {
    return "Tracing{" +
            "idTracing='" + idTracing + '\'' +
            ", id=" + id +
            ", createDate=" + createDate +
            ", comments='" + comments + '\'' +
            ", cancellationReason=" + cancellationReason +
            ", courier=" + courier +
            ", status=" + status +
            ", minutesToDeliver=" + minutesToDeliver +
            ", deliveryOrderId=" + deliveryOrderId +
            ", deliveryStatus=" + deliveryStatus +
            ", providerId=" + providerId +
            ", statusCode='" + statusCode + '\'' +
            ", uuid='" + uuid + '\'' +
            '}';
  }
}
