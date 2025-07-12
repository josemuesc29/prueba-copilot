package com.imaginamos.farmatodo.model.customer;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class Email {
  private Integer idCustomer;
  private Integer idEmail;
  private String email;
  private Boolean newEmail;

  public Integer getIdCustomer() {
    return idCustomer;
  }

  public void setIdCustomer(Integer idCustomer) {
    this.idCustomer = idCustomer;
  }

  public Integer getIdEmail() {
    return idEmail;
  }

  public void setIdEmail(Integer idEmail) {
    this.idEmail = idEmail;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Boolean isNewEmail() {
    return newEmail;
  }

  public void setNewEmail(Boolean newEmail) {
    this.newEmail = newEmail;
  }
}
