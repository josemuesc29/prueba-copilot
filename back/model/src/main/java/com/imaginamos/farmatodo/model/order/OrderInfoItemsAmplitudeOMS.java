package com.imaginamos.farmatodo.model.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class OrderInfoItemsAmplitudeOMS {

    public int item;
    public int store;
    public int quantityRequest;
    public double discount;
    public double value;
    public double totalBilledValue;
    public int id;
    public String mediaDescription;
    public String grayDescription;
    public String departments;
    public String categorie;
    public String subCategory;
    public String brand;
    public String idOrder;
    public int quantitySold;
    public double fullPrice;
    public double offerPrice;
    public String deliveryType;
    public String source;
    private boolean isBilled;

    public int getItem() { return item; }

    public void setItem(int item) { this.item = item; }

    public int getStore() { return store; }

    public void setStore(int store) { this.store = store; }

    public int getQuantityRequest() { return quantityRequest; }

    public void setQuantityRequest(int quantityRequest) { this.quantityRequest = quantityRequest; }

    public double getDiscount() { return discount; }

    public void setDiscount(double discount) { this.discount = discount; }

    public double getValue() { return value; }

    public void setValue(double value) { this.value = value; }

    public double getTotalBilledValue() { return totalBilledValue; }

    public void setTotalBilledValue(double totalBilledValue) { this.totalBilledValue = totalBilledValue; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getMediaDescription() { return mediaDescription; }

    public void setMediaDescription(String mediaDescription) { this.mediaDescription = mediaDescription; }

    public String getGrayDescription() { return grayDescription; }

    public void setGrayDescription(String grayDescription) { this.grayDescription = grayDescription; }

    public String getDepartments() { return departments; }

    public void setDepartments(String departments) { this.departments = departments; }

    public String getCategorie() { return categorie; }

    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getSubCategory() { return subCategory; }

    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getBrand() { return brand; }

    public void setBrand(String brand) { this.brand = brand; }

    public String getIdOrder() { return idOrder; }

    public void setIdOrder(String idOrder) { this.idOrder = idOrder; }

    public int getQuantitySold() { return quantitySold; }

    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }

    public double getFullPrice() { return fullPrice; }

    public void setFullPrice(double fullPrice) { this.fullPrice = fullPrice; }

    public double getOfferPrice() { return offerPrice; }

    public void setOfferPrice(double offerPrice) { this.offerPrice = offerPrice; }

    public String getDeliveryType() { return deliveryType; }

    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getSource() { return source; }

    public void setSource(String source) { this.source = source; }

    public boolean isBilled() { return isBilled; }

    public void setBilled(boolean billed) { isBilled = billed; }

    public String toStringJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
