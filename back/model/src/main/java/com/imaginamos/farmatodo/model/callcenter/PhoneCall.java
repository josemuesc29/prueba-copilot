package com.imaginamos.farmatodo.model.callcenter;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Parent;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class PhoneCall {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idPhoneCall;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<CallCenterUser> idUser;
  private String phone;
  @IgnoreSave
  private String idPhoneCallWebSafe;

  public String getIdPhoneCall() {
    return idPhoneCall;
  }

  public void setIdPhoneCall(String idPhoneCall) {
    this.idPhoneCall = idPhoneCall;
  }

  public Ref<CallCenterUser> getIdUser() {
    return idUser;
  }

  public void setIdUser(Ref<CallCenterUser> idUser) {
    this.idUser = idUser;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getIdPhoneCallWebSafe() {
    return idPhoneCallWebSafe;
  }

  public void setIdPhoneCallWebSafe(String idPhoneCallWebSafe) {
    this.idPhoneCallWebSafe = idPhoneCallWebSafe;
  }
}
