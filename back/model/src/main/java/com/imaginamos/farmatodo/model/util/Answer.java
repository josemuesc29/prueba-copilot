package com.imaginamos.farmatodo.model.util;

import com.imaginamos.farmatodo.model.algolia.CouponPopUpData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by mileniopc on 1/4/17.
 * Property of Imaginamos.
 */

public class Answer {
  private Boolean confirmation;
  private String tokenFarmatodo;
  private String message;
  private Long count;
  private CouponPopUpData CouponPopUp;
  //private CouponPopUpData CouponPopUp = new ArrayList<>();

  public Answer(){}

  public Answer(Boolean confirmation){ this.confirmation = confirmation; }

  public Boolean isConfirmation() {
    return confirmation;
  }

  public void setConfirmation(Boolean confirmation) {
    this.confirmation = confirmation;
  }

  public String getTokenFarmatodo() {
    return tokenFarmatodo;
  }

  public void setTokenFarmatodo(String tokenFarmatodo) {
    this.tokenFarmatodo = tokenFarmatodo;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public Boolean getConfirmation() {
    return confirmation;
  }

  public CouponPopUpData getCouponPopUp() {
    return CouponPopUp;
  }

  public void setCouponPopUp(CouponPopUpData couponPopUp) {
    CouponPopUp = couponPopUp;
  }


}
