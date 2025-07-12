package com.imaginamos.farmatodo.model.order;

import com.imaginamos.farmatodo.model.product.Item;

import java.util.List;

/**
 * Created by USUARIO on 27/01/2017.
 */

public class ShoppingCart {

  private List<DeliveryOrderItem> deliveryOrderItemList;
  private List<Item> highlightedItems;

  public List<DeliveryOrderItem> getDeliveryOrderItemList() {
    return deliveryOrderItemList;
  }

  public void setDeliveryOrderItemList(List<DeliveryOrderItem> deliveryOrderItemList) {
    this.deliveryOrderItemList = deliveryOrderItemList;
  }

  public List<Item> getHighlightedItems() {
    return highlightedItems;
  }

  public void setHighlightedItems(List<Item> highlightedItems) {
    this.highlightedItems = highlightedItems;
  }
}
