package com.imaginamos.farmatodo.model.order;

import com.imaginamos.farmatodo.model.customer.CustomerJSON;

import java.util.List;

public class OrderScanAndGo {

  private DeliveryOrder deliveryOrder;
  private CustomerJSON customer;
  private List<OrderItemScanAndGo> listOrderItem;

  public DeliveryOrder getDeliveryOrder() {
    return deliveryOrder;
  }

  public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
    this.deliveryOrder = deliveryOrder;
  }

  public CustomerJSON getCustomer() {
    return customer;
  }

  public void setCustomer(CustomerJSON customer) {
    this.customer = customer;
  }

  public List<OrderItemScanAndGo> getListOrderItem() {
    return listOrderItem;
  }

  public void setListOrderItem(List<OrderItemScanAndGo> listOrderItem) {
    this.listOrderItem = listOrderItem;
  }
}

