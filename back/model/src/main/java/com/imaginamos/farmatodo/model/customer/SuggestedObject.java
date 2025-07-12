package com.imaginamos.farmatodo.model.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Suggested;

import java.util.List;

public class SuggestedObject {
    private String id;
    private String firstDescription;
    private String secondDescription;
    private String urlImage;
    private long startDate;
    private long endDate;
    private List<Suggested> items;
    private String type;
    private List<Item> product;

    private boolean isPrime;
    @JsonSerialize
    @JsonProperty("orderingNumber")
    private int position;
    private String offerText;


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

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    @JsonProperty("orderingNumber")
    public int getPosition() {
        return position;
    }

    @JsonProperty("orderingNumber")
    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isPrime() {
        return isPrime;
    }

    public void setPrime(boolean prime) {
        isPrime = prime;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toStringJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
