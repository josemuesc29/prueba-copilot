package com.imaginamos.farmatodo.model.util;

import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.categories.Shortcut;
import com.imaginamos.farmatodo.model.categories.SubCategory;
import com.imaginamos.farmatodo.model.location.Prefix;
import com.imaginamos.farmatodo.model.offer.Offer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderV2;
import com.imaginamos.farmatodo.model.order.Rating;
import com.imaginamos.farmatodo.model.product.Highlight;
import com.imaginamos.farmatodo.model.product.Item;

import java.util.List;

/**
 * Created by mileniopc on 12/25/16.
 * Property of Imaginamos.
 */

public class CollectionResponseModelV2 {
  private List<Item> items;
  private List<Department> departmentList;
  private List<SubCategory> subCategoryList;
  private List<Shortcut> shortCutList;
  private List<Highlight> highlightList;
  private List<Offer> offerList;
  private List<Prefix> countries;
  private List<DeliveryOrderV2> activeOrders;
  private List<DeliveryOrderV2> previousOrders;
  private List<Rating> ratings;
  private Long timeStamp;
  private Long totalProducts;
  private String nextPageToken;
  private Long pages;
  private Long currentPage;
  private Long hitsPerPage;
  private String code;

  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }

  public List<Department> getDepartmentList() {
    return departmentList;
  }

  public void setDepartmentList(List<Department> departmentList) {
    this.departmentList = departmentList;
  }

  public Long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public List<SubCategory> getSubCategoryList() {
    return subCategoryList;
  }

  public void setSubCategoryList(List<SubCategory> subCategoryList) {
    this.subCategoryList = subCategoryList;
  }

  public Long getTotalProducts() {
    return totalProducts;
  }

  public void setTotalProducts(Long totalProducts) {
    this.totalProducts = totalProducts;
  }

  public String getNextPageToken() {
    return nextPageToken;
  }

  public void setNextPageToken(String nextPageToken) {
    this.nextPageToken = nextPageToken;
  }

  public List<Shortcut> getShortCutList() {
    return shortCutList;
  }

  public void setShortCutList(List<Shortcut> shortCutList) {
    this.shortCutList = shortCutList;
  }

  public List<Highlight> getHighlightList() {
    return highlightList;
  }

  public void setHighlightList(List<Highlight> highlightList) {
    this.highlightList = highlightList;
  }

  public List<Offer> getOfferList() {
    return offerList;
  }

  public void setOfferList(List<Offer> offerList) {
    this.offerList = offerList;
  }

  public List<Prefix> getCountries() {
    return countries;
  }

  public void setCountries(List<Prefix> countries) {
    this.countries = countries;
  }

  public List<DeliveryOrderV2> getActiveOrders() {
    return activeOrders;
  }

  public List<Rating> getRatings() {
    return ratings;
  }

  public void setRatings(List<Rating> ratings) {
    this.ratings = ratings;
  }

  public void setActiveOrders(List<DeliveryOrderV2> activeOrders) {
    this.activeOrders = activeOrders;
  }

  public List<DeliveryOrderV2> getPreviousOrders() {
    return previousOrders;
  }

  public void setPreviousOrders(List<DeliveryOrderV2> previousOrders) {
    this.previousOrders = previousOrders;
  }

  public Long getPages() {
    return pages;
  }

  public void setPages(Long pages) {
    this.pages = pages;
  }

  public Long getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(Long currentPage) {
    this.currentPage = currentPage;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Long getHitsPerPage() {
    return hitsPerPage;
  }

  public void setHitsPerPage(Long hitsPerPage) {
    this.hitsPerPage = hitsPerPage;
  }
}

