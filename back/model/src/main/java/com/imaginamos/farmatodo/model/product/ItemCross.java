package com.imaginamos.farmatodo.model.product;

import java.util.List;

/**
 * Created by Eric on 28/02/2017.
 */

public class ItemCross {
  private long id;
  private List<Suggested> crossSales;
  private List<Suggested> substitutes;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<Suggested> getCrossSales() {
    return crossSales;
  }

  public void setCrossSales(List<Suggested> crossSales) {
    this.crossSales = crossSales;
  }

  public List<Suggested> getSubstitutes() {
    return substitutes;
  }

  public void setSubstitutes(List<Suggested> substitutes) {
    this.substitutes = substitutes;
  }
}
