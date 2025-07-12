package com.imaginamos.farmatodo.model.product;

import java.util.List;

/**
 * Created by eric on 10/04/17.
 */

public class Products {
  private List<Item> itemList;
  private Long[] items;
  private String keyClient;

  public String getKeyClient() {
    return keyClient;
  }

  public void setKeyClient(String keyClient) {
    this.keyClient = keyClient;
  }

  public List<Item> getItemList() {

    return itemList;
  }

  public void setItemList(List<Item> itemList) {
    this.itemList = itemList;
  }

  public Long[] getItems() {
    return items;
  }

  public void setItems(Long[] items) {
    this.items = items;
  }
}
