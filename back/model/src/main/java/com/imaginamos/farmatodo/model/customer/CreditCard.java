package com.imaginamos.farmatodo.model.customer;

import java.util.List;

/**
 * Created by Daniela Lozano on 3/2/17.
 * Property Imaginamos SAS
 */

public class CreditCard {
  private String creditCardId;
  private String paymentMethod;
  private String maskedNumber;
  private Boolean defaultCard;
  private String customerName;

  private String creditCardNumber;
  private Boolean status;



  public String getCreditCardId() {
    return creditCardId;
  }

  public void setCreditCardId(String creditCardId) {
    this.creditCardId = creditCardId;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getMaskedNumber() {
    return maskedNumber;
  }

  public void setMaskedNumber(String maskedNumber) {
    this.maskedNumber = maskedNumber;
  }

  public Boolean getDefaultCard() {
    return defaultCard;
  }

  public void setDefaultCard(Boolean defaultCard) {
    this.defaultCard = defaultCard;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCreditCardNumber() {
    return creditCardNumber;
  }

  public void setCreditCardNumber(String creditCardNumber) {
    this.creditCardNumber = creditCardNumber;
  }


  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }
}
