package com.imaginamos.farmatodo.model.product;

import java.util.List;

/**
 * Created by eric on 8/05/17.
 */

public class ProductGroup {
  private String token;
  private String tokenIdWebSafe;
  private List<Suggested> items;
  private long idStoreGroup;

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

  public List<Suggested> getItems() {
    return items;
  }

  public void setItems(List<Suggested> items) {
    this.items = items;
  }

  public long getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(long idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
  }
}
