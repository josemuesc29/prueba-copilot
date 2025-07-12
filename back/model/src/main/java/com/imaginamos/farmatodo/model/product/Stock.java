package com.imaginamos.farmatodo.model.product;

/**
 * Created by USUARIO on 18/01/2017.
 */

public class Stock {

  private int group;
  private int stock;
  private int taxRate;
  private int fullValue;
  private boolean hasComplexOffer;
  private BestDeal bestDeal;

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int totalStock) {
    this.stock = totalStock;
  }

  public int getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(int taxRate) {
    this.taxRate = taxRate;
  }

  public int getFullValue() {
    return fullValue;
  }

  public void setFullValue(int fullValue) {
    this.fullValue = fullValue;
  }

  public boolean isHasComplexOffer() {
    return hasComplexOffer;
  }

  public void setHasComplexOffer(boolean hasComplexOffer) {
    this.hasComplexOffer = hasComplexOffer;
  }

  public BestDeal getBestDeal() {
    return bestDeal;
  }

  public void setBestDeal(BestDeal bestDeal) {
    this.bestDeal = bestDeal;
  }
}
