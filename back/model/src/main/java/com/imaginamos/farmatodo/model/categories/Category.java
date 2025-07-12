package com.imaginamos.farmatodo.model.categories;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.cms.CategoryPhoto;

import java.util.List;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class Category extends Classification {

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<Department> idClassificationLevel1;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int parent;
  @IgnoreSave
  private List<SubCategory> children;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int priority;
  @IgnoreSave
  private Boolean isImage;
  @IgnoreSave
  private CategoryPhoto categoryPhoto;
  @IgnoreSave
  private String textoSEO;

  public Category() {
  }

  public Ref<Department> getIdClassificationLevel1() {
    return idClassificationLevel1;
  }

  public void setIdClassificationLevel1(Ref<Department> idClassificationLevel1) {
    this.idClassificationLevel1 = idClassificationLevel1;
  }

  public int getParent() {
    return parent;
  }

  public void setParent(int parent) {
    this.parent = parent;
  }

  public List<SubCategory> getChildren() {
    return children;
  }

  public void setChildren(List<SubCategory> children) {
    this.children = children;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public Boolean getImage() {
    return isImage;
  }

  public void setImage(Boolean image) {
    isImage = image;
  }

  public CategoryPhoto getCategoryPhoto() {
    return categoryPhoto;
  }

  public void setCategoryPhoto(CategoryPhoto categoryPhoto) {
    this.categoryPhoto = categoryPhoto;
  }


  public String getTextoSEO() {
    return textoSEO;
  }

  public void setTextoSEO(String textoSEO) {
    this.textoSEO = textoSEO;
  }
}
