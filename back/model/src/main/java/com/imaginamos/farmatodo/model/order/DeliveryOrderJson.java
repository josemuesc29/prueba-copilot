package com.imaginamos.farmatodo.model.order;


import java.util.List;

/**
 * Created by Eric on 13/03/2017.
 */

public class DeliveryOrderJson {
  private List<ItemOnShop> items;

  public List<ItemOnShop> getItems() {
    return items;
  }

  public void setItems(List<ItemOnShop> items) {
    this.items = items;
  }
}
