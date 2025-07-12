package com.imaginamos.farmatodo.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

/**
 * Created by mileniopc on 11/2/16.
 * Property of Imaginamos.
 */

@Entity
public class UserPass {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idUserPass;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<User> owner;
  private String password;
  @Index
  private boolean active;
  private Date createAt;
  private Date inactiveAt;
  @IgnoreSave
  private String idUserPassWebSafe;

  public String getIdUserPass() {
    return idUserPass;
  }

  public void setIdUserPass(String idUserPass) {
    this.idUserPass = idUserPass;
  }

  public Ref<User> getOwner() {
    return owner;
  }

  public void setOwner(Ref<User> owner) {
    this.owner = owner;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Date getCreateAt() {
    return createAt;
  }

  public void setCreateAt(Date createAt) {
    this.createAt = createAt;
  }

  public Date getInactiveAt() {
    return inactiveAt;
  }

  public void setInactiveAt(Date inactiveAt) {
    this.inactiveAt = inactiveAt;
  }

  public String getIdUserPassWebSafe() {
    return idUserPassWebSafe;
  }

  public void setIdUserPassWebSafe(String idUserPassWebSafe) {
    this.idUserPassWebSafe = idUserPassWebSafe;
  }
}
