package com.imaginamos.farmatodo.model.product;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import java.util.List;

/**
 * Created by Eric on 23/2/2017.
 */

@Entity
public class Substitutes {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idItem;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<Item> idItemParent;
  private boolean anywaySelling;
  private String barcode;
  private String brand;

  @Index
  private Long idClassification;
  private Double fullPrice;
  private String grayDescription;
  @Index
  private boolean highlight;
  @Index
  private long id;
  private boolean isGeneric;
  private String mediaDescription;
  private String mediaImageUrl;
  private int offerPercentage;
  private Double offerPrice;
  private String offerText;
  private String offerDescription;
  @Index
  private boolean outstanding = false;
  private String requirePrescription;
  private long sales;
  private long spaces;
  private String status;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int idStoreGroup;
  private int taxRate;
  @Index
  private int totalStock;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Suggested> suggested;

  // Nuevos campos creados para el tag del portal
  private String marca;
  private String categorie;
  private List<String> departments;
  private String subCategory;
  private String supplier;
  @IgnoreSave
  private List<ItemFilter> filters;
  private boolean onlyOnline;
  private boolean subscribeAndSave;

  @IgnoreSave
  private String itemUrl;

  public boolean isSubscribeAndSave() {
    return subscribeAndSave;
  }

  public void setSubscribeAndSave(boolean subscribeAndSave) {
    this.subscribeAndSave = subscribeAndSave;
  }

  public boolean isOnlyOnline() {
    return onlyOnline;
  }

  public void setOnlyOnline(boolean onlyOnline) {
    this.onlyOnline = onlyOnline;
  }

  public String getIdItem() {
    return idItem;
  }

  public void setIdItem(String idItem) {
    this.idItem = idItem;
  }

  public Ref<Item> getIdItemParent() {
    return idItemParent;
  }

  public void setIdItemParent(Ref<Item> idItemParent) {
    this.idItemParent = idItemParent;
  }

  public boolean isAnywaySelling() {
    return anywaySelling;
  }

  public void setAnywaySelling(boolean anywaySelling) {
    this.anywaySelling = anywaySelling;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public Long getIdClassification() {
    return idClassification;
  }

  public void setIdClassification(Long idClassification) {
    this.idClassification = idClassification;
  }

  public Double getFullPrice() {
    return fullPrice;
  }

  public void setFullPrice(Double fullPrice) {
    this.fullPrice = fullPrice;
  }

  public String getGrayDescription() {
    return grayDescription;
  }

  public void setGrayDescription(String grayDescription) {
    this.grayDescription = grayDescription;
  }

  public boolean isHighlight() {
    return highlight;
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public boolean isGeneric() {
    return isGeneric;
  }

  public void setGeneric(boolean generic) {
    isGeneric = generic;
  }

  public String getMediaDescription() {
    return mediaDescription;
  }

  public void setMediaDescription(String mediaDescription) {
    this.mediaDescription = mediaDescription;
  }

  public String getMediaImageUrl() {
    return mediaImageUrl;
  }

  public void setMediaImageUrl(String mediaImageUrl) {
    this.mediaImageUrl = mediaImageUrl;
  }

  public int getOfferPercentage() {
    return offerPercentage;
  }

  public void setOfferPercentage(int offerPercentage) {
    this.offerPercentage = offerPercentage;
  }

  public Double getOfferPrice() {
    return offerPrice;
  }

  public void setOfferPrice(Double offerPrice) {
    this.offerPrice = offerPrice;
  }

  public boolean isOutstanding() {
    return outstanding;
  }

  public void setOutstanding(boolean outstanding) {
    this.outstanding = outstanding;
  }

  public String getRequirePrescription() {
    return requirePrescription;
  }

  public void setRequirePrescription(String requirePrescription) {
    this.requirePrescription = requirePrescription;
  }

  public long getSales() {
    return sales;
  }

  public void setSales(long sales) {
    this.sales = sales;
  }

  public long getSpaces() {
    return spaces;
  }

  public void setSpaces(long spaces) {
    this.spaces = spaces;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(int idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
  }

  public int getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(int taxRate) {
    this.taxRate = taxRate;
  }

  public int getTotalStock() {
    return totalStock;
  }

  public void setTotalStock(int totalStock) {
    this.totalStock = totalStock;
  }

  public String getOfferText() {
    return offerText;
  }

  public void setOfferText(String offerText) {
    this.offerText = offerText;
  }

  public String getOfferDescription() {
    return offerDescription;
  }

  public void setOfferDescription(String offerDescription) {
    this.offerDescription = offerDescription;
  }

  public List<Suggested> getSuggested() {
    return suggested;
  }

  public void setSuggested(List<Suggested> suggested) {
    this.suggested = suggested;
  }

  public String getMarca() { return marca; }

  public void setMarca(String marca) { this.marca = marca; }

  public String getCategorie() { return categorie; }

  public void setCategorie(String categorie) { this.categorie = categorie; }

  public List<String> getDepartments() { return departments; }

  public void setDepartments(List<String> departments) { this.departments = departments; }

  public String getSubCategory() { return subCategory; }

  public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

  public String getSupplier() { return supplier; }

  public void setSupplier(String supplier) { this.supplier = supplier; }

  public List<ItemFilter> getFilters() { return filters; }

  public void setFilters(List<ItemFilter> filters) { this.filters = filters; }

  public String getItemUrl() {
    return itemUrl;
  }

  public void setItemUrl(String itemUrl) {
    this.itemUrl = itemUrl;
  }
}
