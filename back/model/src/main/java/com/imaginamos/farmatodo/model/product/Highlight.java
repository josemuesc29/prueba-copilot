package com.imaginamos.farmatodo.model.product;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.imaginamos.farmatodo.model.categories.HighlightClass;

import java.util.List;

/**
 * Created by Eric on 21/02/2017.
 */

@Entity
public class Highlight {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String highlightId;
  @Index
  private Long id;
  private String firstDescription;
  private String secondDescription;
  private String offerDescription;
  private String type;
  private String urlImage;
  private Long startDate;
  private Long endDate;
  @Index
  private Integer orderingNumber;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Suggested> items;
  @IgnoreSave
  private Long item;
  @IgnoreSave
  private List<Item> product;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private List<HighlightClass> categories;
  private String offerText;

  public String getHighlightId() {
    return highlightId;
  }

  public void setHighlightId(String highlightId) {
    this.highlightId = highlightId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public Long getStartDate() {
    return startDate;
  }

  public void setStartDate(Long startDate) {
    this.startDate = startDate;
  }

  public Long getEndDate() {
    return endDate;
  }

  public void setEndDate(Long endDate) {
    this.endDate = endDate;
  }

  public List<Suggested> getItems() {
    return items;
  }

  public void setItems(List<Suggested> items) {
    this.items = items;
  }

  public Integer getOrderingNumber() {
    return orderingNumber;
  }

  public void setOrderingNumber(Integer orderingNumber) {
    this.orderingNumber = orderingNumber;
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

  public List<HighlightClass> getCategories() {
    return categories;
  }

  public void setCategories(List<HighlightClass> categories) {
    this.categories = categories;
  }

  public String getOfferDescription() {
    return offerDescription;
  }

  public void setOfferDescription(String offerDescription) {
    this.offerDescription = offerDescription;
  }

  public String getOfferText() {
    return offerText;
  }

  public void setOfferText(String offerText) {
    this.offerText = offerText;
  }

  @Override
  public String toString() {
    return "Highlight{" +
            "highlightId='" + highlightId + '\'' +
            ", id=" + id +
            ", firstDescription='" + firstDescription + '\'' +
            ", secondDescription='" + secondDescription + '\'' +
            ", offerDescription='" + offerDescription + '\'' +
            ", type='" + type + '\'' +
            ", urlImage='" + urlImage + '\'' +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", orderingNumber=" + orderingNumber +
            ", items=" + items +
            ", item=" + item +
            ", product=" + product +
            ", categories=" + categories +
            ", offerText=" + offerText +
            '}';
  }
}
