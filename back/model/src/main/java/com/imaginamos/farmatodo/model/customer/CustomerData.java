package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Suggested;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created Cristhian Rodriguez
 * Property of Farmatodo
 */

public class CustomerData {
    private Integer id;
    private String firstName;
    private String lastName;
    private String gender;
    private BigInteger documentNumber;
    private String documentType;
    private String country;
    private int idStoreGroup;
    private int shopingCartNumber;
    private List<Banner> banners = new ArrayList<>();
    private List<Item> previousItems = new ArrayList<>();
    private List<Address> addresses;
    private String email;
    private String phone;
    private List<Suggested> purchases;
    private List<CreditCard> creditCards;
    private String registeredBy;
    private int activeOrders;
    private boolean vip;
    private boolean hasCreditCards;

    public CustomerData() {}

    public CustomerData(CustomerJSON customerJSON) {
        this.id = customerJSON.getId();
        this.firstName = customerJSON.getFirstName();
        this.lastName  = customerJSON.getLastName();
        this.gender    = customerJSON.getGender();
        this.documentNumber = customerJSON.getDocumentNumber();
        this.documentType   = customerJSON.getDocumentType();
        this.country        = customerJSON.getCountry();
        this.idStoreGroup   = customerJSON.getIdStoreGroup();
        this.shopingCartNumber = customerJSON.getShopingCartNumber();
        this.banners = Objects.nonNull(customerJSON.getBanners()) && !customerJSON.getBanners().isEmpty() ? customerJSON.getBanners() : null;
        this.previousItems = new ArrayList<>();
        this.addresses = Objects.nonNull(customerJSON.getAddresses()) && !customerJSON.getAddresses().isEmpty() ? customerJSON.getAddresses() : null;
        this.email     = customerJSON.getEmail();
        this.phone     = customerJSON.getPhone();
        this.purchases = customerJSON.getPurchases();
        this.creditCards  = customerJSON.getCreditCards();
        this.registeredBy = customerJSON.getRegisteredBy();
        this.activeOrders = customerJSON.getActiveOrders();
        this.vip = customerJSON.isVip();
        this.hasCreditCards = Objects.nonNull(customerJSON.getCreditCards()) && !customerJSON.getCreditCards().isEmpty();
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

    public List<Banner> getBanners() {
        return banners;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
    }

    public List<Item> getPreviousItems() {
        return previousItems;
    }

    public void setPreviousItems(List<Item> previousItems) {
        this.previousItems = previousItems;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
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

    public List<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(List<CreditCard> creditCards) {
        this.creditCards = creditCards;
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

    @Override
    public String toString() {
        return "CustomerData{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", documentNumber=" + documentNumber +
                ", documentType='" + documentType + '\'' +
                ", country='" + country + '\'' +
                ", idStoreGroup=" + idStoreGroup +
                ", shopingCartNumber=" + shopingCartNumber +
                ", banners=" + banners +
                ", previousItems=" + previousItems +
                ", addresses=" + addresses +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", purchases=" + purchases +
                ", creditCards=" + creditCards +
                ", registeredBy='" + registeredBy + '\'' +
                ", activeOrders=" + activeOrders + '\'' +
                ", vip= "+ vip +
                '}';
    }
}
