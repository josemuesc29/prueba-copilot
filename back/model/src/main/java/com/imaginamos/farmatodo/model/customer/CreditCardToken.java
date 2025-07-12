package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.user.Token;

/**
 * Created by Eric on 8/03/2017.
 */

public class CreditCardToken {
  //Customer information
  private long id;
  private String customerName;
  private String documentNumber;

  //credit card information
  private String creditCardTokenId;
  private String paymentMethod;
  private String maskedNumber;

  private Token token;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getCreditCardTokenId() {
    return creditCardTokenId;
  }

  public void setCreditCardTokenId(String creditCardTokenId) {
    this.creditCardTokenId = creditCardTokenId;
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

  public Token getToken() {
    return token;
  }

  public void setToken(Token token) {
    this.token = token;
  }
}
