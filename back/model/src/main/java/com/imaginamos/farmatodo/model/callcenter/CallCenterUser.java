package com.imaginamos.farmatodo.model.callcenter;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.user.User;

import java.util.Date;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class CallCenterUser extends User {
  /*@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCallCenterUser;*/
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<CallCenterProfile> idCallCenterProfile;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Key<User> userKey;
  private String login;
  private String name;
  private String password;
  private boolean status;
  private String title;
  private String photoUrl;
  private String occupation;
  private Date lastUpdateDate;
  /*private Token token;
  private String firebaseToken;*/
  @IgnoreSave
  private String idCallCenterUserWebSafe;

    /*public String getIdCallCenterUser() {
        return idCallCenterUser;
    }

    public void setIdCallCenterUser(String idCallCenterUser) {
        this.idCallCenterUser = idCallCenterUser;
    }*/

  public Ref<CallCenterProfile> getIdCallCenterProfile() {
    return idCallCenterProfile;
  }

  public void setIdCallCenterProfile(Ref<CallCenterProfile> idCallCenterProfile) {
    this.idCallCenterProfile = idCallCenterProfile;
  }

  public Key<User> getUserKey() {
    return userKey;
  }

  public void setUserKey(Key<User> userKey) {
    this.userKey = userKey;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public String getOccupation() {
    return occupation;
  }

  public void setOccupation(String occupation) {
    this.occupation = occupation;
  }

  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  public void setLastUpdateDate(Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
  }

    /*public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }*/

  public String getIdCallCenterUserWebSafe() {
    return idCallCenterUserWebSafe;
  }

  public void setIdCallCenterUserWebSafe(String idCallCenterUserWebSafe) {
    this.idCallCenterUserWebSafe = idCallCenterUserWebSafe;
  }
}
