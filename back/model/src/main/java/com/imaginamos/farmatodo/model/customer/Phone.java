package com.imaginamos.farmatodo.model.customer;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class Phone {
  private Integer idCustomer;
  private Integer idPhone;
  private String phoneNo;
  private String phoneType;
  private Boolean newPhone;

  public Integer getIdCustomer() {
    return idCustomer;
  }

  public void setIdCustomer(Integer idCustomer) {
    this.idCustomer = idCustomer;
  }

  public Integer getIdPhone() {
    return idPhone;
  }

  public void setIdPhone(Integer idPhone) {
    this.idPhone = idPhone;
  }

  public String getPhoneNo() {
    return phoneNo;
  }

  public void setPhoneNo(String phoneNo) {
    this.phoneNo = phoneNo;
  }

  public String getPhoneType() {
    return phoneType;
  }

  public void setPhoneType(String phoneType) {
    this.phoneType = phoneType;
  }

  public Boolean isNewPhone() {
    return newPhone;
  }

  public void setNewPhone(Boolean newPhone) {
    this.newPhone = newPhone;
  }
}
