package com.imaginamos.farmatodo.model.order;

/**
 * Created by Admin on 15/05/2017.
 */

public class ItemOnShop {
  private long id;
  private long quantitySold;
  private String observations;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getQuantitySold() {
    return quantitySold;
  }

  public void setQuantitySold(long quantitySold) {
    this.quantitySold = quantitySold;
  }

  public String getObservations() { return observations; }

  public void setObservations(String observations) { this.observations = observations; }
}
