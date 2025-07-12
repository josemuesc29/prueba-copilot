package com.imaginamos.farmatodo.model.order;

/**
 * Created by USUARIO on 01/02/2017.
 */

public class ResponseUpdateDeliveryOrder {

  private int status;
  private String total;
  private String subtotal;
  private int time;

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getTotal() {
    return total;
  }

  public void setTotal(String total) {
    this.total = total;
  }

  public String getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(String subtotal) {
    this.subtotal = subtotal;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }
}
