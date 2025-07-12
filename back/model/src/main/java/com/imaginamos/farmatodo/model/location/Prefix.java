package com.imaginamos.farmatodo.model.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Prefix {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idPrefix;
  private String name;
  private String prefix;
  private String flagUrl;

  public String getIdPrefix() {
    return idPrefix;
  }

  public void setIdPrefix(String idPrefix) {
    this.idPrefix = idPrefix;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getFlagUrl() {
    return flagUrl;
  }

  public void setFlagUrl(String flagUrl) {
    this.flagUrl = flagUrl;
  }
}
