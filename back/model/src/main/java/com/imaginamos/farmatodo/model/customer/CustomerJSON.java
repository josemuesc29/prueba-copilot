package com.imaginamos.farmatodo.model.customer;


import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.discounts.Component;
import com.imaginamos.farmatodo.model.product.Highlight;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Suggested;
import com.imaginamos.farmatodo.model.product.Suggestion;
import com.imaginamos.farmatodo.model.user.Token;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class CustomerJSON {
  private Integer id;
  private String idCustomerWebSafe;
  private String firstName;
  private String lastName;
  private String gender;
  private BigInteger documentNumber;
  private String documentType;
  private String country;
  private String city;
  private Token token;
  private Double latitude;
  private Double longitude;
  private int idStoreGroup;
  private int shopingCartNumber;
  private List<Banner> banners = new ArrayList<>();
  private List<Suggestion> suggestedProducts = new ArrayList<>();
  private List<Item> previousItems = new ArrayList<>();
  private List<Highlight> highlightedItems = new ArrayList<>();
  private List<Address> addresses;
  private String email;
  private String phone;
  private List<Component> components;
  private List<Suggestion> suggested;
  private List<Suggested> purchases;
  private List<CreditCard> creditCards;
  private String profileImageUrl;
  private String registeredBy;
  private int activeOrders;
  private List<Interests> interests;
  private Long lifeMileNumber;
  boolean vip;
  private Long creationDate;
  private Long atomId;
  private Boolean isUserCall;
  private List<CustomerPhotoData> photos;
  private String analyticsUUID ;

  public CustomerJSON() {
  }

  public CustomerJSON(Integer id, String idCustomerWebSafe, String firstName, String lastName, String gender, BigInteger documentNumber, String documentType, String country, String city, Token token, Double latitude, Double longitude, int idStoreGroup, int shopingCartNumber, List<Banner> banners, List<Suggestion> suggestedProducts, List<Item> previousItems, List<Highlight> highlightedItems, List<Address> addresses, String email, String phone, List<Component> components, List<Suggestion> suggested, List<Suggested> purchases, List<CreditCard> creditCards, String profileImageUrl, String registeredBy, int activeOrders, List<Interests> interests, Long lifeMileNumber, boolean vip, Long creationDate, Long atomId) {
    this.id = id;
    this.idCustomerWebSafe = idCustomerWebSafe;
    this.firstName = firstName;
    this.lastName = lastName;
    this.gender = gender;
    this.documentNumber = documentNumber;
    this.documentType = documentType;
    this.country = country;
    this.city = city;
    this.token = token;
    this.latitude = latitude;
    this.longitude = longitude;
    this.idStoreGroup = idStoreGroup;
    this.shopingCartNumber = shopingCartNumber;
    this.banners = banners;
    this.suggestedProducts = suggestedProducts;
    this.previousItems = previousItems;
    this.highlightedItems = highlightedItems;
    this.addresses = addresses;
    this.email = email;
    this.phone = phone;
    this.components = components;
    this.suggested = suggested;
    this.purchases = purchases;
    this.creditCards = creditCards;
    this.profileImageUrl = profileImageUrl;
    this.registeredBy = registeredBy;
    this.activeOrders = activeOrders;
    this.interests = interests;
    this.lifeMileNumber = lifeMileNumber;
    this.vip = vip;
    this.creationDate = creationDate;
    this.atomId = atomId;
  }

  public CustomerJSON(CustomerDataResponse customer, Integer id) {
    this.id = id;
    this.firstName = customer.getFirstname();
    this.lastName = customer.getLastname();
    this.documentNumber = BigInteger.valueOf(customer.getDocumentNumber());
    this.documentType = String.valueOf(customer.getDocumentTypeId());
    this.country = customer.getCountryId();
    this.email = customer.getEmail();
    this.phone = customer.getPhone();
    this.creationDate = customer.getCreationDate();
    this.registeredBy = customer.getRegisteredBy();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getIdCustomerWebSafe() {
    return idCustomerWebSafe;
  }

  public void setIdCustomerWebSafe(String idCustomerWebSafe) {
    this.idCustomerWebSafe = idCustomerWebSafe;
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

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Token getToken() {
    return token;
  }

  public void setToken(Token token) {
    this.token = token;
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

  public List<Highlight> getHighlightedItems() {
    return highlightedItems;
  }

  public void setHighlightedItems(List<Highlight> highlightedItems) {
    this.highlightedItems = highlightedItems;
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

  public List<Component> getComponents() {
    return components;
  }

  public void setComponents(List<Component> components) {
    this.components = components;
  }

  public List<Suggestion> getSuggested() {
    return suggested;
  }

  public void setSuggested(List<Suggestion> suggested) {
    this.suggested = suggested;
  }

  public List<Suggested> getPurchases() {
    return purchases;
  }

  public void setPurchases(List<Suggested> purchases) {
    this.purchases = purchases;
  }

  public List<CreditCard> getCreditCards() {
    return creditCards;
  }

  public void setCreditCards(List<CreditCard> creditCards) {
    this.creditCards = creditCards;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
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

  public List<Interests> getInterests() {
    return interests;
  }

  public void setInterests(List<Interests> interests) {
    this.interests = interests;
  }

  public Long getLifeMileNumber() { return lifeMileNumber; }

  public void setLifeMileNumber(Long lifeMileNumber) { this.lifeMileNumber = lifeMileNumber; }

  public boolean isVip() {
    return vip;
  }

  public void setVip(boolean vip) {
    this.vip = vip;
  }

  public Long getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  public Long getAtomId() {
    return atomId;
  }

  public void setAtomId(Long atomId) {
    this.atomId = atomId;
  }

  public Boolean getUserCall() {
    return isUserCall;
  }

  public void setUserCall(Boolean userCall) {
    isUserCall = userCall;
  }

  public List<CustomerPhotoData> getPhotos() {
    return photos;
  }

  public void setPhotos(List<CustomerPhotoData> photos) {
    this.photos = photos;
  }

  public String getAnalyticsUUID() {
    return analyticsUUID;
  }

  public void setAnalyticsUUID(String analyticsUUID) {
    this.analyticsUUID = analyticsUUID;
  }

  @Override
  public String toString() {
    return "CustomerJSON{" +
            "id=" + id +
            ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", gender='" + gender + '\'' +
            ", documentNumber=" + documentNumber +
            ", documentType='" + documentType + '\'' +
            ", country='" + country + '\'' +
            ", city='" + city + '\'' +
            ", token=" + token +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", idStoreGroup=" + idStoreGroup +
            ", shopingCartNumber=" + shopingCartNumber +
            ", banners=" + banners +
            ", suggestedProducts=" + suggestedProducts +
            ", previousItems=" + previousItems +
            ", highlightedItems=" + highlightedItems +
            ", addresses=" + addresses +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", components=" + components +
            ", suggested=" + suggested +
            ", purchases=" + purchases +
            ", creditCards=" + creditCards +
            ", profileImageUrl='" + profileImageUrl + '\'' +
            ", registeredBy='" + registeredBy + '\'' +
            ", activeOrders=" + activeOrders +
            ", interests=" + interests +
            ", lifeMileNumber=" + lifeMileNumber +
            ", isUserCall=" + isUserCall +
            '}';
  }

}
