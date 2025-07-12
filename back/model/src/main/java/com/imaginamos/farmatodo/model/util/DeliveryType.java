package com.imaginamos.farmatodo.model.util;

public enum DeliveryType {

  EXPRESS("EXPRESS", 26),
  NATIONAL("NATIONAL", 1000),
  ENVIALOYA("ENVIALOYA",1001),
  SUBSCRIPTION("SUBSCRIPTION", 26),
  SCANANDGO("SCANANDGO", 26),
  PRIME("PRIME", 26),
  PROVIDER("PROVIDER", 1000);

  DeliveryType(String deliveryType, int defaultStore) {
    this.deliveryType = deliveryType;
    this.defaultStore = defaultStore;
  }

  private String deliveryType;

  private int defaultStore;

  public String getDeliveryType() {
    return this.deliveryType;
  }

  public int getDefaultStore() {
    return defaultStore;
  }

  public static DeliveryType getDeliveryType(String deliveryType){
    for (DeliveryType record : values()) {
      if (record.getDeliveryType().equals(deliveryType)) {
        return record;
      }
    }
    return null;
  }

  public void setDeliveryType(String deliveryType) {
    this.deliveryType = deliveryType;
  }

  public String toString() {
    return this.deliveryType;
  }

}
