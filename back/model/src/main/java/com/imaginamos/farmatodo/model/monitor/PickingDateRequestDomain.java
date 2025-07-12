package com.imaginamos.farmatodo.model.monitor;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class PickingDateRequestDomain {

  private Long orderId;
  private Long employeeNumber;
  private String correoUsuario;
  private String rol;
  private String employeeName;
  private String pickingDate;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Long getEmployeeNumber() {
    return employeeNumber;
  }

  public void setEmployeeNumber(Long employeeNumber) {
    this.employeeNumber = employeeNumber;
  }

  public String getCorreoUsuario() {
    return correoUsuario;
  }

  public void setCorreoUsuario(String correoUsuario) {
    this.correoUsuario = correoUsuario;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public String getPickingDate() {
    return pickingDate;
  }

  public void setPickingDate(String pickingDate) {
    this.pickingDate = pickingDate;
  }

}
