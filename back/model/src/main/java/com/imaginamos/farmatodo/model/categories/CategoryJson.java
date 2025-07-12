package com.imaginamos.farmatodo.model.categories;

import java.util.List;

/**
 * Created by Eric on 2/03/2017.
 */

public class CategoryJson {
  private String token;
  private String tokenIdWebSafe;
  private Integer categoryId;
  private List<Integer> filterIdList;
  private int idStoreGroup;
  private String cursor;
  private Boolean order;
  private Boolean orderDirection;
  private int idOffer;
  private int lowPrice;
  private int highPrice;
  private Boolean subscribeAndSave;


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

  public Integer getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Integer categoryId) {
    this.categoryId = categoryId;
  }

  public int getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(int idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public Boolean getOrder() {
    return order;
  }

  public void setOrder(Boolean order) {
    this.order = order;
  }

  public List<Integer> getFilterIdList() {
    return filterIdList;
  }

  public void setFilterIdList(List<Integer> filterIdList) {
    this.filterIdList = filterIdList;
  }

  public Boolean getOrderDirection() {
    return orderDirection;
  }

  public void setOrderDirection(Boolean orderDirection) {
    this.orderDirection = orderDirection;
  }

  public int getIdOffer() { return idOffer; }

  public void setIdOffer(int idOffer) { this.idOffer = idOffer; }

  public int getLowPrice() { return lowPrice; }

  public void setLowPrice(int lowPrice) { this.lowPrice = lowPrice; }

  public int getHighPrice() { return highPrice; }

  public void setHighPrice(int highPrice) { this.highPrice = highPrice; }

  public Boolean getSubscribeAndSave() {
    return subscribeAndSave;
  }

  public void setSubscribeAndSave(Boolean subscribeAndSave) {
    this.subscribeAndSave = subscribeAndSave;
  }
}
