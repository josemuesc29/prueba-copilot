package com.imaginamos.farmatodo.model.product;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by eric on 12/05/17.
 */

@Entity
public class ItemGroup {
  @Id
  private String idItemGroup;
  private String description;

  public String getIdItemGroup() {
    return idItemGroup;
  }

  public void setIdItemGroup(String idItemGroup) {
    this.idItemGroup = idItemGroup;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
