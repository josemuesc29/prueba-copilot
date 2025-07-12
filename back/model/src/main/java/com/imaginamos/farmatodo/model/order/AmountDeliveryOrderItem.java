package com.imaginamos.farmatodo.model.order;

import java.util.HashMap;

/**
 * Created by USUARIO on 01/02/2017.
 */

public class AmountDeliveryOrderItem {

  private HashMap<String, Integer> amount;
  private String phone;
  private String address;

  public HashMap<String, Integer> getAmount() {
    return amount;
  }

  public void setAmount(HashMap<String, Integer> amount) {
    this.amount = amount;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
