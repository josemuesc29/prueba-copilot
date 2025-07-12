package com.imaginamos.farmatodo.model.callcenter;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class CallCenterProfile {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCallCenterProfile;
  @Index
  private String name;
  private String description;
  private String photoURL;
  @IgnoreSave
  private String idCallCenterProfileWebSafe;

  public String getIdCallCenterProfile() {
    return idCallCenterProfile;
  }

  public void setIdCallCenterProfile(String idCallCenterProfile) {
    this.idCallCenterProfile = idCallCenterProfile;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPhotoURL() {
    return photoURL;
  }

  public void setPhotoURL(String photoURL) {
    this.photoURL = photoURL;
  }

  public String getIdCallCenterProfileWebSafe() {
    return idCallCenterProfileWebSafe;
  }

  public void setIdCallCenterProfileWebSafe(String idCallCenterProfileWebSafe) {
    this.idCallCenterProfileWebSafe = idCallCenterProfileWebSafe;
  }
}
