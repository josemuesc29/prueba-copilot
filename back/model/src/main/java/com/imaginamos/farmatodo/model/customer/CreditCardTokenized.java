package com.imaginamos.farmatodo.model.customer;


/**
 * Created by eric on 25/04/17.
 */

public class CreditCardTokenized {
  private String code;
  private String error;
  private CreditCardTokenResponse creditCardToken;
  private String transactionResponse;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public CreditCardTokenResponse getCreditCardToken() {
    return creditCardToken;
  }

  public void setCreditCardToken(CreditCardTokenResponse creditCardToken) {
    this.creditCardToken = creditCardToken;
  }

  public String getTransactionResponse() {
    return transactionResponse;
  }

  public void setTransactionResponse(String transactionResponse) {
    this.transactionResponse = transactionResponse;
  }
}
