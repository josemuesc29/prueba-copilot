package com.imaginamos.farmatodo.model.order;

public class SendOrder {

  private Long orderId;
  private Integer postfix;
  private boolean skipAttemptsValidation;
  private String text;
  private Integer attempt;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Integer getPostfix() {
    return postfix;
  }

  public void setPostfix(Integer postfix) {
    this.postfix = postfix;
  }

  public boolean isSkipAttemptsValidation() {
    return skipAttemptsValidation;
  }

  public void setSkipAttemptsValidation(boolean skipAttemptsValidation) {
    this.skipAttemptsValidation = skipAttemptsValidation;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Integer getAttempt() {
    return attempt;
  }

  public void setAttempt(Integer attempt) {
    this.attempt = attempt;
  }
}
