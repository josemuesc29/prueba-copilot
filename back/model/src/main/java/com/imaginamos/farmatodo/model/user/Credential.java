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
public class Credential {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCredential;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<User> owner;
  @Index
  private String email;
  private Date createAt;
  private Boolean confirmed;
  @Index
  private Boolean status;
  private Date lastLogin;
  private Date lastRecover;
  private Date expirationRecovery;
  @IgnoreSave
  private String idCredentialWebSafe;

  public String getIdCredential() {
    return idCredential;
  }

  public void setIdCredential(String idCredential) {
    this.idCredential = idCredential;
  }

  public Ref<User> getOwner() {
    return owner;
  }

  public void setOwner(Ref<User> owner) {
    this.owner = owner;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Date getCreateAt() {
    return createAt;
  }

  public void setCreateAt(Date createAt) {
    this.createAt = createAt;
  }

  public Boolean getConfirmed() {
    return confirmed;
  }

  public void setConfirmed(Boolean confirmed) {
    this.confirmed = confirmed;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }

  public Date getLastRecover() {
    return lastRecover;
  }

  public void setLastRecover(Date lastRecover) {
    this.lastRecover = lastRecover;
  }

  public Date getExpirationRecovery() {
    return expirationRecovery;
  }

  public void setExpirationRecovery(Date expirationRecovery) {
    this.expirationRecovery = expirationRecovery;
  }

  public String getIdCredentialWebSafe() {
    return idCredentialWebSafe;
  }

  public void setIdCredentialWebSafe(String idCredentialWebSafe) {
    this.idCredentialWebSafe = idCredentialWebSafe;
  }

  @Override
  public String toString() {
    return "Credential{" +
            "idCredential='" + idCredential + '\'' +
            ", owner=" + owner +
            ", email='" + email + '\'' +
            ", createAt=" + createAt +
            ", confirmed=" + confirmed +
            ", status=" + status +
            ", lastLogin=" + lastLogin +
            ", lastRecover=" + lastRecover +
            ", expirationRecovery=" + expirationRecovery +
            ", idCredentialWebSafe='" + idCredentialWebSafe + '\'' +
            '}';
  }
}
