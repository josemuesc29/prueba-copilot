package com.imaginamos.farmatodo.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mileniopc on 11/2/16.
 * Property of Imaginamos.
 */

@Entity
public class User {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idUser;
  private String role;
  private String tokenFirebase;
  @Index
  private String uidFirebase;
  @IgnoreSave
  private Token token;
  @Index
  private int id;
  @IgnoreSave
  private String idUserWebSafe;
  @Index
  private String idFacebook;
  @Index
  private String idGoogle;
  @Index
  private long lastLogin;

  public String getIdUser() {
    return idUser;
  }

  public void setIdUser(String idUser) {
    this.idUser = idUser;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getTokenFirebase() {
    return tokenFirebase;
  }

  public void setTokenFirebase(String tokenFirebase) {
    this.tokenFirebase = tokenFirebase;
  }

  public Token getToken() {
    return token;
  }

  public void setToken(Token token) {
    this.token = token;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getIdUserWebSafe() {
    return idUserWebSafe;
  }

  public void setIdUserWebSafe(String idUserWebSafe) {
    this.idUserWebSafe = idUserWebSafe;
  }

  public String getIdFacebook() {
    return idFacebook;
  }

  public void setIdFacebook(String idFacebook) {
    this.idFacebook = idFacebook;
  }

  public String getIdGoogle() {
    return idGoogle;
  }

  public void setIdGoogle(String idGoogle) {
    this.idGoogle = idGoogle;
  }

  public long getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(long lastLogin) {
    this.lastLogin = lastLogin;
  }

  public String getUidFirebase() {
    return uidFirebase;
  }

  public void setUidFirebase(String uidFirebase) {
    this.uidFirebase = uidFirebase;
  }

  @Override
  public String toString() {
    return "User{" +
            "idUser='" + idUser + '\'' +
            ", role='" + role + '\'' +
            ", tokenFirebase='" + tokenFirebase + '\'' +
            ", token=" + token +
            ", id=" + id +
            ", idUserWebSafe='" + idUserWebSafe + '\'' +
            ", idFacebook='" + idFacebook + '\'' +
            ", idGoogle='" + idGoogle + '\'' +
            ", lastLogin=" + lastLogin +
            '}';
  }

  public String toStringJson(){
    return new Gson().toJson(this);
  }
}
