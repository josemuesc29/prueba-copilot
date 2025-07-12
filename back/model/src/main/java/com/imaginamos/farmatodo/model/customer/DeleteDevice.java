package com.imaginamos.farmatodo.model.customer;

/**
 * Created by Admin on 07/06/2017.
 */

public class DeleteDevice {
  private String token;
  private String tokenIdWebSafe;
  private String idCustomerWebSafe;
  private String deviceId;
  private String firebaseTokenDevice;
  private Boolean isWeb;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTokenIdWebSafe() {
    return tokenIdWebSafe;
  }

  public void setTokenIdWebSafe(String tokenIdWebSafe) {
    this.tokenIdWebSafe = tokenIdWebSafe;
  }

  public String getIdCustomerWebSafe() {
    return idCustomerWebSafe;
  }

  public void setIdCustomerWebSafe(String idCustomerWebSafe) {
    this.idCustomerWebSafe = idCustomerWebSafe;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getFirebaseTokenDevice() {
    return firebaseTokenDevice;
  }

  public void setFirebaseTokenDevice(String firebaseTokenDevice) {
    this.firebaseTokenDevice = firebaseTokenDevice;
  }

  public Boolean getWeb() {
    return isWeb;
  }

  public void setWeb(Boolean web) {
    isWeb = web;
  }
}
