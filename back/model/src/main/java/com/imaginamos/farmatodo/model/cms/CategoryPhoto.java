package com.imaginamos.farmatodo.model.cms;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

@Entity
public class CategoryPhoto {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCategoryPhoto;
  @Index
  private Long idDepartment;
  @Index
  private Integer imagePosition;
  private String imageUrl;
  private Boolean redirect;
  private String redirectUrl;

  @IgnoreSave
  private String tokenIdWebSafe;
  @IgnoreSave
  private String token;
  @IgnoreSave
  private String idPhotoWebSafe;

  public String getIdCategoryPhoto() {
    return idCategoryPhoto;
  }

  public void setIdCategoryPhoto(String idCategoryPhoto) {
    this.idCategoryPhoto = idCategoryPhoto;
  }

  public Long getIdDepartment() {
    return idDepartment;
  }

  public void setIdDepartment(Long idDepartment) {
    this.idDepartment = idDepartment;
  }

  public Integer getImagePosition() {
    return imagePosition;
  }

  public void setImagePosition(Integer imagePosition) {
    this.imagePosition = imagePosition;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Boolean getRedirect() {
    return redirect;
  }

  public void setRedirect(Boolean redirect) {
    this.redirect = redirect;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public String getTokenIdWebSafe() {
    return tokenIdWebSafe;
  }

  public void setTokenIdWebSafe(String tokenIdWebSafe) {
    this.tokenIdWebSafe = tokenIdWebSafe;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getIdPhotoWebSafe() {
    return idPhotoWebSafe;
  }

  public void setIdPhotoWebSafe(String idPhotoWebSafe) {
    this.idPhotoWebSafe = idPhotoWebSafe;
  }

  @Override
  public String toString() {
    return "CategoryPhoto{" +
            "idCategoryPhoto='" + idCategoryPhoto + '\'' +
            ", idDepartment=" + idDepartment +
            ", imagePosition=" + imagePosition +
            ", imageUrl='" + imageUrl + '\'' +
            ", redirect=" + redirect +
            ", redirectUrl='" + redirectUrl + '\'' +
            ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
            ", token='" + token + '\'' +
            ", idPhotoWebSafe='" + idPhotoWebSafe + '\'' +
            '}';
  }
}
