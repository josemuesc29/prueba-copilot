package com.imaginamos.farmatodo.model.categories;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class SubCategory extends Classification {

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<Category> idClassificationLevel2;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int parent;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int priority;
  @IgnoreSave
  private String textoSEO;

  public SubCategory() {
  }

  public Ref<Category> getIdClassificationLevel2() {
    return idClassificationLevel2;
  }

  public void setIdClassificationLevel2(Ref<Category> idClassificationLevel2) {
    this.idClassificationLevel2 = idClassificationLevel2;
  }

  public int getParent() {
    return parent;
  }

  public void setParent(int parent) {
    this.parent = parent;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }


  public String getTextoSEO() {
    return textoSEO;
  }

  public void setTextoSEO(String textoSEO) {
    this.textoSEO = textoSEO;
  }
}
