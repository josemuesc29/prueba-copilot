package com.imaginamos.farmatodo.model.order;

/**
 * Created by diego.poveda on 11/12/2017.
 */

public class Assessment {

  private Integer id;
  private String description;

  public Assessment() {
  }

  public Assessment(Integer id) {
    this.id = id;
  }

  public Assessment(Integer id, String description) {
    this.id = id;
    this.description = description;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
