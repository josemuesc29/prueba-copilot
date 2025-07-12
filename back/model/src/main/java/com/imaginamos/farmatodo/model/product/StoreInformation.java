package com.imaginamos.farmatodo.model.product;


/**
 * Created by eric on 9/05/17.
 */

public class StoreInformation {
  private long storeGroupId;
  private Double fullPrice;
  private Double offerPrice;
  private String offerText;
  private String offerDescription;
  private long stock;


  public long getStoreGroupId() {
    return storeGroupId;
  }

  public void setStoreGroupId(long storeGroupId) {
    this.storeGroupId = storeGroupId;
  }

  public Double getFullPrice() {
    return fullPrice;
  }

  public void setFullPrice(Double fullPrice) {
    this.fullPrice = fullPrice;
  }

  public Double getOfferPrice() {
    return offerPrice;
  }

  public void setOfferPrice(Double offerPrice) {
    this.offerPrice = offerPrice;
  }

  public String getOfferText() {
    return offerText;
  }

  public void setOfferText(String offerText) {
    this.offerText = offerText;
  }

  public String getOfferDescription() {
    return offerDescription;
  }

  public void setOfferDescription(String offerDescription) {
    this.offerDescription = offerDescription;
  }

  public long getStock() {
    return stock;
  }

  public void setStock(long stock) {
    this.stock = stock;
  }

}
