package com.imaginamos.farmatodo.model.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.product.ItemOrder;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;
import com.imaginamos.farmatodo.model.provider.SupplierShippingCostMarketplace;
import main.java.com.imaginamos.farmatodo.model.order.ShoppingCartCourierCostResp;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

/**
 * Created by Eric on 27/02/2017.
 */

public class OrderJson {
  private int globalDiscount;
  private String commentDiscount;
  private int registeredDiscount;
  private int deliveryValue;
  private double weight;
  private double lowerRangeWeight;
  private double topRangeWeight;
  private double subTotalPrice;
  private double offerPrice;
  private int providerDeliveryValue;
  private List<ItemAlgolia> items;
  private List<ProviderOrder> providers;
  private List<SupplierShippingCostMarketplace> supplierShippingCost;
  private Double marketplaceShippingCostTotal;
  private ShoppingCartCourierCostResp shoppingCartCourierCost;

  public int getGlobalDiscount() {
    return globalDiscount;
  }

  public void setGlobalDiscount(int globalDiscount) {
    this.globalDiscount = globalDiscount;
  }

  public String getCommentDiscount() {
    return commentDiscount;
  }

  public void setCommentDiscount(String commentDiscount) {
    this.commentDiscount = commentDiscount;
  }

  public int getRegisteredDiscount() {
    return registeredDiscount;
  }

  public void setRegisteredDiscount(int registeredDiscount) {
    this.registeredDiscount = registeredDiscount;
  }

  public int getDeliveryValue() {
    return deliveryValue;
  }

  public void setDeliveryValue(int deliveryValue) {
    this.deliveryValue = deliveryValue;
  }

  public double getWeight() {
    return weight;
  }

  public double getLowerRangeWeight() {
    return lowerRangeWeight;
  }

  public void setLowerRangeWeight(double lowerRangeWeight) {
    this.lowerRangeWeight = lowerRangeWeight;
  }

  public double getTopRangeWeight() {
    // TODO: Borrar este fragmento de código cuando se actualicen las aplicaciones
    if(Objects.nonNull(topRangeWeight) && topRangeWeight > 0){
      NumberFormat formato = new DecimalFormat("##.###");
      topRangeWeight = Double.valueOf(formato.format(Math.floor(topRangeWeight)));
    }
    return topRangeWeight;
  }

  public void setTopRangeWeight(double topRangeWeight) {
    this.topRangeWeight = topRangeWeight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public List<ItemAlgolia> getItems() {
    return items;
  }

  public void setItems(List<ItemAlgolia> items) {
    this.items = items;
  }

  public int getProviderDeliveryValue() { return providerDeliveryValue; }

  public void setProviderDeliveryValue(int providerDeliveryValue) { this.providerDeliveryValue = providerDeliveryValue; }

  public List<ProviderOrder> getProviders() { return providers; }

  public void setProviders(List<ProviderOrder> providers) { this.providers = providers; }

  public double getSubTotalPrice() {
    return subTotalPrice;
  }

  public void setSubTotalPrice(double subTotalPrice) {
    this.subTotalPrice = subTotalPrice;
  }

  public double getOfferPrice() {
    return offerPrice;
  }

  public void setOfferPrice(double offerPrice) {
    this.offerPrice = offerPrice;
  }
  
  public List<SupplierShippingCostMarketplace> getSupplierShippingCost() {
    return supplierShippingCost;
  }
  
  public void setSupplierShippingCost(List<SupplierShippingCostMarketplace> supplierShippingCost) {
    this.supplierShippingCost = supplierShippingCost;
  }
  
  public Double getMarketplaceShippingCostTotal() {
    return marketplaceShippingCostTotal;
  }
  
  public void setMarketplaceShippingCostTotal(Double marketplaceShippingCostTotal) {
    this.marketplaceShippingCostTotal = marketplaceShippingCostTotal;
  }

  public ShoppingCartCourierCostResp getShoppingCartCourierCost() {
    return shoppingCartCourierCost;
  }
  
  public void setShoppingCartCourierCost(ShoppingCartCourierCostResp shoppingCartCourierCost) {
    this.shoppingCartCourierCost = shoppingCartCourierCost;
  }

  @Override
  public String toString() {
    return "OrderJson{" +
        "globalDiscount=" + globalDiscount +
        ", commentDiscount='" + commentDiscount + '\'' +
        ", registeredDiscount=" + registeredDiscount +
        ", deliveryValue=" + deliveryValue +
        ", weight=" + weight +
        ", lowerRangeWeight=" + lowerRangeWeight +
        ", topRangeWeight=" + topRangeWeight +
        ", providerDeliveryValue="+ providerDeliveryValue +
        ", items=" + items +
        ", providers=" +providers +
        '}';
  }
/*  public String toStringJson() {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String json = null;
    try {
      json = ow.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return json;
  }*/
public String toStringJson() {
  Gson gson = new Gson();
  return gson.toJson(this);
}

}
