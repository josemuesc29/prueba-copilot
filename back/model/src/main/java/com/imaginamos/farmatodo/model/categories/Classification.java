package com.imaginamos.farmatodo.model.categories;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

/**
 * Created by sebastian vargas on 05/01/2017.
 */

@Entity
public class Classification {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  protected String idClassification;
  @Index
  protected long id;
  protected String name;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  protected String status;
  protected String url;
  @IgnoreSave
  protected String idClassificationWebSafe;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  protected int parent;
  @IgnoreSave
  private List<FilterName> filters;


  public int getParent() {
    return parent;
  }

  public void setParent(int parent) {
    this.parent = parent;
  }

    /*public List<Classification> getFilterNames() {
        return children;
    }

    public void setFilterNames(List<Classification> children) {
        this.children = children;
    }*/

  public String getIdClassification() {
    return idClassification;
  }

  public void setIdClassification(String idClassification) {
    this.idClassification = idClassification;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getIdClassificationWebSafe() {
    return idClassificationWebSafe;
  }

  public void setIdClassificationWebSafe(String idClassificationWebSafe) {
    this.idClassificationWebSafe = idClassificationWebSafe;
  }

  public List<FilterName> getFilters() {
    return filters;
  }

  public void setFilters(List<FilterName> filters) {
    this.filters = filters;
  }
}
