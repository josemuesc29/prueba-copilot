package com.imaginamos.farmatodo.model.util;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by USUARIO on 05/07/2017.
 */

@Entity
public class VersionControl {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String itemId;
  private int controlNumber;
  @Index
  private String tableControl;
  @IgnoreSave
  private long token;
  @IgnoreSave
  private int revision;

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }


  public String getTableControl() {
    return tableControl;
  }

  public void setTableControl(String tableControl) {
    this.tableControl = tableControl;
  }

  public long getToken() {
    return token;
  }

  public void setToken(long token) {
    this.token = token;
  }

  public void setControlNumber(int controlNumber) {
    this.controlNumber = controlNumber;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getControlNumber() {
    return controlNumber;
  }
}
