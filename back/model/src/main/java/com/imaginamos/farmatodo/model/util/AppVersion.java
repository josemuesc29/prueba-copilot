package com.imaginamos.farmatodo.model.util;


import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Admin on 20/06/2017.
 */


@Entity
public class AppVersion {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String appVersionId;
  @IgnoreSave
  private String keyClient;
  private String appVersion;
  @Index
  private String platform;
  private boolean update;
  private String updateCopy;
  private int intVersion;

  public String getAppVersionId() {
    return appVersionId;
  }

  public void setAppVersionId(String appVersionId) {
    this.appVersionId = appVersionId;
  }

  public String getKeyClient() {
    return keyClient;
  }

  public void setKeyClient(String keyClient) {
    this.keyClient = keyClient;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public boolean getUpdate() {
    return update;
  }

  public void setUpdate(boolean update) {
    this.update = update;
  }

  public String getUpdateCopy() {
    return updateCopy;
  }

  public void setUpdateCopy(String updateCopy) {
    this.updateCopy = updateCopy;
  }

  public int getIntVersion() {
    return intVersion;
  }

  public void setIntVersion(int intVersion) {
    this.intVersion = intVersion;
  }
}
