package com.imaginamos.farmatodo.model.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Admin on 18/05/2017.
 */

@Entity
public class Country {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idCountry;
  @Index
  private String defaultCityId;
  @Index
  private String id;
  private String language;
  private String name;
  @Index
  private boolean status;

  public String getIdCountry() {
    return idCountry;
  }

  public void setIdCountry(String idCountry) {
    this.idCountry = idCountry;
  }

  public String getDefaultCityId() {
    return defaultCityId;
  }

  public void setDefaultCityId(String defaultCityId) {
    this.defaultCityId = defaultCityId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }
}
