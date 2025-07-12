package com.imaginamos.farmatodo.model.product;

/**
 * Created by Eric on 22/2/2017.
 */

public class BestDeal {
  private String customerType;
  private int offerValue;
  private String offerText;
  private String offerDescription;

  public String getCustomerType() {
    return customerType;
  }

  public void setCustomerType(String customerType) {
    this.customerType = customerType;
  }

  public int getOfferValue() {
    return offerValue;
  }

  public void setOfferValue(int offerValue) {
    this.offerValue = offerValue;
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
}
