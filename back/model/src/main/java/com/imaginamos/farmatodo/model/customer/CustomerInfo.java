package com.imaginamos.farmatodo.model.customer;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Segment;

/**
 * Created by mileniopc on 11/2/16.
 * Property of Imaginamos.
 */

public class CustomerInfo {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCustomer;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Segment> idSegment;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<User> userKey;
  private String idConsumerFarmatodo;

  public String getIdCustomer() {
    return idCustomer;
  }

  public void setIdCustomer(String idCustomer) {
    this.idCustomer = idCustomer;
  }

  public Key<Segment> getIdSegment() {
    return idSegment;
  }

  public void setIdSegment(Key<Segment> idSegment) {
    this.idSegment = idSegment;
  }

  public Key<User> getUserKey() {
    return userKey;
  }

  public void setUserKey(Key<User> userKey) {
    this.userKey = userKey;
  }

  public String getIdConsumerFarmatodo() {
    return idConsumerFarmatodo;
  }

  public void setIdConsumerFarmatodo(String idConsumerFarmatodo) {
    this.idConsumerFarmatodo = idConsumerFarmatodo;
  }
}
