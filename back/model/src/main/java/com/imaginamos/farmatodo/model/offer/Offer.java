package com.imaginamos.farmatodo.model.offer;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Suggested;

import java.util.List;

/**
 * Created by eric on 2/05/17.
 */

@Entity
public class Offer {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String offerId;
  @Index
  private long id;
  private String firstDescription;
  private String secondDescription;
  private String offerDescription;
  private String type;
  private String urlImage;
  private long startDate;
  private long endDate;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Suggested> items;
  @IgnoreSave
  private Long item;
  @IgnoreSave
  private List<Item> product;
  @Index
  private Integer orderingNumber;

  public String getOfferId() {
    return offerId;
  }

  public void setOfferId(String offerId) {
    this.offerId = offerId;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUrlImage() {
    return urlImage;
  }

  public void setUrlImage(String urlImage) {
    this.urlImage = urlImage;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public Long getItem() {
    return item;
  }

  public void setItem(Long item) {
    this.item = item;
  }

  public List<Item> getProduct() {
    return product;
  }

  public void setProduct(List<Item> product) {
    this.product = product;
  }

  public String getOfferDescription() {
    return offerDescription;
  }

  public void setOfferDescription(String offerDescription) {
    this.offerDescription = offerDescription;
  }

  public Integer getOrderingNumber() {
    return orderingNumber;
  }

  public void setOrderingNumber(Integer orderingNumber) {
    this.orderingNumber = orderingNumber;
  }
}
