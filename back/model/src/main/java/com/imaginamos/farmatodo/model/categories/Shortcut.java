package com.imaginamos.farmatodo.model.categories;

/**
 * Created by eric on 11/05/17.
 */

public class Shortcut {
  private long id;
  private String description;
  private String imageURL;
  private String redirectURL;
  private long clasificationId;
  private int orderingNumber;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  public String getRedirectURL() {
    return redirectURL;
  }

  public void setRedirectURL(String redirectURL) {
    this.redirectURL = redirectURL;
  }

  public long getClasificationId() {
    return clasificationId;
  }

  public void setClasificationId(long clasificationId) {
    this.clasificationId = clasificationId;
  }

  public int getOrderingNumber() {
    return orderingNumber;
  }

  public void setOrderingNumber(int orderingNumber) {
    this.orderingNumber = orderingNumber;
  }
}
