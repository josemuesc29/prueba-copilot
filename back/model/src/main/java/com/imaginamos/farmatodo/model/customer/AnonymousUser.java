package com.imaginamos.farmatodo.model.customer;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Suggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniela Lozano on 2/28/17.
 * Property Imaginamos SAS
 */

@Entity
public class AnonymousUser {
  @Id
  String idAnonymousUser;
  private Double latitude;
  private Double longitude;
  private List<Suggestion> suggestedProducts = new ArrayList<>();
  private List<Item> previousItems = new ArrayList<>();

  public String getIdAnonymousUser() {
    return idAnonymousUser;
  }

  public void setIdAnonymousUser(String idAnonymousUser) {
    this.idAnonymousUser = idAnonymousUser;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public List<Suggestion> getSuggestedProducts() {
    return suggestedProducts;
  }

  public void setSuggestedProducts(List<Suggestion> suggestedProducts) {
    this.suggestedProducts = suggestedProducts;
  }

  public List<Item> getPreviousItems() {
    return previousItems;
  }

  public void setPreviousItems(List<Item> previousItems) {
    this.previousItems = previousItems;
  }
}
