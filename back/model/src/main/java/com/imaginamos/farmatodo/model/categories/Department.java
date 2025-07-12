package com.imaginamos.farmatodo.model.categories;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class Department extends Classification {

  private String primaryColor;
  private String secondColor;
  private List<Images> images;
  @IgnoreSave
  private List<Category> children;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int priority;
  @IgnoreSave
  private String textoSEO;

  public Department() {
  }

  public String getPrimaryColor() {
    return primaryColor;
  }

  public void setPrimaryColor(String primaryColor) {
    this.primaryColor = primaryColor;
  }

  public String getSecondColor() {
    return secondColor;
  }

  public void setSecondColor(String secondColor) {
    this.secondColor = secondColor;
  }

  public List<Category> getChildren() {
    return children;
  }

  public void setChildren(List<Category> children) {
    this.children = children;
  }

  public List<Images> getImages() {
    return images;
  }

  public void setImages(List<Images> images) {
    this.images = images;
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
