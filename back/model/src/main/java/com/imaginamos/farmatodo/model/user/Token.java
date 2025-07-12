package com.imaginamos.farmatodo.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Parent;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by mileniopc on 10/27/16.
 * Property of Imaginamos.
 */

@Entity
public class Token {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String tokenId;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<User> owner;
  private String token;
  private Date tokenExp;
  private String refreshToken;
  @IgnoreSave
  private String tokenIdWebSafe;

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public Ref<User> getOwner() {
    return owner;
  }

  public void setOwner(Ref<User> owner) {
    this.owner = owner;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getTokenExp() {
    return tokenExp;
  }

  public void setTokenExp(int days) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DAY_OF_YEAR, days);
    this.tokenExp = calendar.getTime();
  }

  public void setTokenExpDate(Date tokenExp) {
    this.tokenExp = tokenExp;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenIdWebSafe() {
    return tokenIdWebSafe;
  }

  public void setTokenIdWebSafe(String tokenIdWebSafe) {
    this.tokenIdWebSafe = tokenIdWebSafe;
  }

  @Override
  public String toString() {
    return "Token{" +
            "tokenId='" + tokenId + '\'' +
            ", owner=" + owner +
            ", token='" + token + '\'' +
            ", tokenExp=" + tokenExp +
            ", refreshToken='" + refreshToken + '\'' +
            ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
            '}';
  }
}
