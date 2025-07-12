package com.imaginamos.farmatodo.model.categories;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * Created by RentAdvisor on 2/2/17.
 */

@Entity
public class Filter {

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  protected String idClassification;
  @Index
  protected long id;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<FilterName> idFilterName;
  @Index
  private String value; //name

  public Ref<FilterName> getIdFilterName() {
    return idFilterName;
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

  public void setIdFilterName(Ref<FilterName> idFilterName) {
    this.idFilterName = idFilterName;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
