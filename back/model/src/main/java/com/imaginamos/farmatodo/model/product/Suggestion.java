package com.imaginamos.farmatodo.model.product;

import java.util.List;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class Suggestion {
  private String id;
  private String firstDescription;
  private String secondDescription;
  private String urlImage;
  private long startDate;
  private long endDate;
  private List<Suggested> items;
  private String type;
  private List<Item> product;
  private int orderingNumber;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstDescription() {
    return firstDescription;
  }

  public void setFirstDescription(String firstDescription) {
    this.firstDescription = firstDescription;
  }

  public String getSecondDescription() {
    return secondDescription;
  }

  public void setSecondDescription(String secondDescription) {
    this.secondDescription = secondDescription;
  }

  public String getUrlImage() {
    return urlImage;
  }

  public void setUrlImage(String urlImage) {
    this.urlImage = urlImage;
  }

  public long getStartDate() {
    return startDate;
  }

  public void setStartDate(long startDate) {
    this.startDate = startDate;
  }

  public long getEndDate() {
    return endDate;
  }

  public void setEndDate(long endDate) {
    this.endDate = endDate;
  }

  public List<Suggested> getItems() {
    return items;
  }

  public void setItems(List<Suggested> items) {
    this.items = items;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Item> getProduct() {
    return product;
  }

  public void setProduct(List<Item> product) {
    this.product = product;
  }

  public int getOrderingNumber() {
    return orderingNumber;
  }

  public void setOrderingNumber(int orderingNumber) {
    this.orderingNumber = orderingNumber;
  }
}
