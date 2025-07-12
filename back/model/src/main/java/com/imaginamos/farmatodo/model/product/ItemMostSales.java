package com.imaginamos.farmatodo.model.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.categories.Department;

import java.util.List;

@Entity
public class ItemMostSales {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String itemMostSalesId;
  @Parent
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Ref<Department> departmentRef;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Suggested> suggested;

  public String getItemMostSalesId() {
    return itemMostSalesId;
  }

  public void setItemMostSalesId(String itemMostSalesId) {
    this.itemMostSalesId = itemMostSalesId;
  }

  public Ref<Department> getDepartmentRef() {
    return departmentRef;
  }

  public void setDepartmentRef(Ref<Department> departmentRef) {
    this.departmentRef = departmentRef;
  }

  public List<Suggested> getSuggested() {
    return suggested;
  }

  public void setSuggested(List<Suggested> suggested) {
    this.suggested = suggested;
  }
  public String toStringJson() {
    Gson g = new Gson();
    return g.toJson(this);
  }
}
