package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.favorite.Favorite;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Suggested;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CustomerOnlyData {

    private Integer id;
    private String firstName;
    private String lastName;
    private String gender;
    private BigInteger documentNumber;
    private String documentType;
    private String country;
    private int idStoreGroup;
    private int shopingCartNumber;
    private List<Item> previousItems = new ArrayList<>();
    private String email;
    private String phone;
    private String registeredBy;
    private int activeOrders;
    private List<Suggested> purchases;
    private boolean vip;
    private List<CustomerPhotoData> photos;
    private boolean hasCreditCards;
    private String lastLoginEmail;
    private List<Favorite> favorites;
    private String analyticsUUID;

    public CustomerOnlyData() {
    }

    public String getAnalyticsUUID() {
        return analyticsUUID;
    }

    public void setAnalyticsUUID(String analyticsUUID) {
        this.analyticsUUID = analyticsUUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigInteger getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(BigInteger documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getIdStoreGroup() {
        return idStoreGroup;
    }

    public void setIdStoreGroup(int idStoreGroup) {
        this.idStoreGroup = idStoreGroup;
    }

    public int getShopingCartNumber() {
        return shopingCartNumber;
    }

    public void setShopingCartNumber(int shopingCartNumber) {
        this.shopingCartNumber = shopingCartNumber;
    }

    public List<Item> getPreviousItems() {
        return previousItems;
    }

    public void setPreviousItems(List<Item> previousItems) {
        this.previousItems = previousItems;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public int getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(int activeOrders) {
        this.activeOrders = activeOrders;
    }

    public List<Suggested> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Suggested> purchases) {
        this.purchases = purchases;
    }

    public boolean isVip() { return vip; }

    public void setVip(boolean vip) { this.vip = vip; }

    public List<CustomerPhotoData> getPhotos() {
        return photos;
    }

    public void setPhotos(List<CustomerPhotoData> photos) {
        this.photos = photos;
    }

    public boolean isHasCreditCards() {
        return hasCreditCards;
    }

    public void setHasCreditCards(boolean hasCreditCards) {
        this.hasCreditCards = hasCreditCards;
    }

    public String getLastLoginEmail() {
        return lastLoginEmail;
    }

    public void setLastLoginEmail(String lastLoginEmail) {
        this.lastLoginEmail = lastLoginEmail;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }

    @Override
    public String toString() {
        return "CustomerOnlyData{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", documentNumber=" + documentNumber +
                ", documentType='" + documentType + '\'' +
                ", country='" + country + '\'' +
                ", idStoreGroup=" + idStoreGroup +
                ", shopingCartNumber=" + shopingCartNumber +
                ", previousItems=" + previousItems +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", registeredBy='" + registeredBy + '\'' +
                ", activeOrders=" + activeOrders +
                ", purchases=" + purchases +
                ", vip=" + vip +
                ", photos=" + photos +
                ", hasCreditCards=" + hasCreditCards +
                '}';
    }
}
