package com.imaginamos.farmatodo.model.categories;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class FilterName {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  protected String idClassification;
  @Index
  protected long id;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Long idCategory;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int parent;
  @IgnoreSave
  private List<Filter> values;
  @Index
  private String filter; //name

  public FilterName() {
  }

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

  public Long getIdCategory() {
    return idCategory;
  }

  public void setIdCategory(Long idCategory) {
    this.idCategory = idCategory;
  }

  public int getParent() {
    return parent;
  }

  public void setParent(int parent) {
    this.parent = parent;
  }

  public List<Filter> getValues() {
    return values;
  }

  public void setValues(List<Filter> values) {
    this.values = values;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }
}
