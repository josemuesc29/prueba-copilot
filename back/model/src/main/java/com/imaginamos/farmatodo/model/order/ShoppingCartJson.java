package com.imaginamos.farmatodo.model.order;


import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.monitor.OrderItem;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.List;
import java.util.Map;

/**
 * Created by Eric on 27/02/2017.
 */

public class ShoppingCartJson {
  private String token;
  private String tokenIdWebSafe;
  private String idCustomerWebSafe;
  private int id;
  private int idStoreGroup;
  private String source;
  private ShoppingCart shoppingCart;
  private String storeSelectMode;
  private String address;
  private DeliveryType deliveryType;
  private long idOrder;
  private List<OrderItem> orderItems;
  private long idItem;
  private Float tip;
  private Integer paymentCardId;
  private String daneCodeCustomer;
  private String addressCustomer;
  private List<Integer> nearbyStores;

  public Integer getPaymentCardId() {
    return paymentCardId;
  }

  public void setPaymentCardId(int paymentCardId) {
    this.paymentCardId = paymentCardId;
  }

  private Long farmaCredits;

  public Map<String, Object> getTalonOneData() {
    return talonOneData;
  }

  public void setTalonOneData(Map<String, Object> talonOneData) {
    this.talonOneData = talonOneData;
  }

  private Map<String, Object> talonOneData;

  public Long getFarmaCredits() {
    return farmaCredits;
  }

  public void setFarmaCredits(Long farmaCredits) {
    this.farmaCredits = farmaCredits;
  }

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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(int idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public ShoppingCart getShoppingCart() {
    return shoppingCart;
  }

  public void setShoppingCart(ShoppingCart shoppingCart) {
    this.shoppingCart = shoppingCart;
  }

  public DeliveryType getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(DeliveryType deliveryType) {
    this.deliveryType = deliveryType;
  }

  public long getIdOrder() {
    return idOrder;
  }

  public void setIdOrder(long idOrder) {
    this.idOrder = idOrder;
  }

  public List<OrderItem> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(List<OrderItem> orderItems) {
    this.orderItems = orderItems;
  }

  public long getIdItem() { return idItem; }

  public void setIdItem(long idItem) { this.idItem = idItem; }

  public Float getTip() {
    return tip;
  }

  public void setTip(Float tip) {
    this.tip = tip;
  }

  

  public String getDaneCodeCustomer() {
    return daneCodeCustomer;
  }

  public void setDaneCodeCustomer(String daneCodeCustomer) {
    this.daneCodeCustomer = daneCodeCustomer;
  }

  public String getAddressCustomer() {
    return addressCustomer;
  }

  public void setAddressCustomer(String addressCustomer) {
    this.addressCustomer = addressCustomer;
  }
  public List<Integer> getNearbyStores() {
      return nearbyStores;
  }

  public void setNearbyStores(List<Integer> nearbyStores) {
      this.nearbyStores = nearbyStores;
  }

  public String toStringJson() {
    return new Gson().toJson(this);
  }
}
