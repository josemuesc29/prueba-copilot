package com.imaginamos.farmatodo.model.product;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

/**
 * Created by Eric on 27/02/2017.
 */

@Deprecated
public class ItemOrder {
  private long item;
  private int quantityRequested;
  private int discount;
  private int fullPrice;
  private int price;
  private int calculatedPrice;
  private int quantityBonus;
  private boolean access;
  private int itemDeliveryPrice;


  public long getItem() {
    return item;
  }

  public void setItem(long item) {
    this.item = item;
  }

  public int getQuantityRequested() {
    return quantityRequested;
  }

  public void setQuantityRequested(int quantityRequested) {
    this.quantityRequested = quantityRequested;
  }

  public int getDiscount() {
    return discount;
  }

  public void setDiscount(int discount) {
    this.discount = discount;
  }

  public int getFullPrice() {
    return fullPrice;
  }

  public void setFullPrice(int fullPrice) {
    this.fullPrice = fullPrice;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getCalculatedPrice() {
    return calculatedPrice;
  }

  public void setCalculatedPrice(int calculatedPrice) {
    this.calculatedPrice = calculatedPrice;
  }

  public int getQuantityBonus() {
    return quantityBonus;
  }

  public void setQuantityBonus(int quantityBonus) {
    this.quantityBonus = quantityBonus;
  }

  public boolean getAccess() {
    return access;
  }

  public void setAccess(boolean access) {
    this.access = access;
  }

  public int getItemDeliveryPrice() { return itemDeliveryPrice; }

  public void setItemDeliveryPrice(int itemDeliveryPrice) { this.itemDeliveryPrice = itemDeliveryPrice; }

  @Override
  public String toString() {
    return "ItemOrder{" +
            "item=" + item +
            ", quantityRequested=" + quantityRequested +
            ", discount=" + discount +
            ", fullPrice=" + fullPrice +
            ", price=" + price +
            ", calculatedPrice=" + calculatedPrice +
            ", quantityBonus=" + quantityBonus +
            ", access=" + access +
            ", itemDeliveryPrice=" + itemDeliveryPrice +
            '}';
  }
}
