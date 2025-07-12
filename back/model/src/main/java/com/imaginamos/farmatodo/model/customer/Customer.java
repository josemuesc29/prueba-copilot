package com.imaginamos.farmatodo.model.customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.user.User;

import java.util.List;
import java.util.Objects;

/**
 * Created by mileniopc on 10/25/16.
 * Property of Imaginamos.
 */

public class Customer extends User {

  private String documentNumber;
  private Integer documentType;
  private String firstName;
  private String lastName;
  private String gender;
  private String countryId;
  private Boolean communicationAllowance;
  private String source;
  private String password;
  private String oldPassword;
  private String idCustomerWebSafe;
  private String deviceId;
  private String firebaseTokenDevice;
  private String tokenFarmatodo;
  private Double latitude;
  private Double longitude;
  private String city;
  private String profileImageUrl;
  private Boolean termsAndConditions;
  private Boolean dataManagement;
  private String email;
  private String phone;
  private List<Address> addresses;
  private String tokenFacebook;
  private String tokenGoogle;
  private String uidFirebase;
  private int idStoreGroup;
  private String registeredBy;
  private String deliveryType;
  private String cursor;
  private List<Interests> interests;
  private List<CustomerPhoneNumber> phoneNumbers;
  private Integer documentTypeId;
  private String registeredByCall;
  private String analyticsUUID;

  public Customer() {
  }

  public Customer(int id,String documentNumber, String firstName, String lastName, String gender, String phone, String profileImageUrl) {
    this.setId(id);
    this.documentNumber = documentNumber;
    this.firstName = firstName;
    this.lastName = lastName;
    this.gender = gender;
    this.phone = phone;
    if (Objects.nonNull(profileImageUrl))
        this.profileImageUrl = profileImageUrl;
  }

    public Integer getDocumentTypeId() {
    return documentTypeId;
  }

  public void setDocumentTypeId(Integer documentTypeId) {
    this.documentTypeId = documentTypeId;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public List<CustomerPhoneNumber> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<CustomerPhoneNumber> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
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

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }


  public Boolean isCommunicationAllowance() {
    return communicationAllowance;
  }

  public void setCommunicationAllowance(Boolean communicationAllowance) {
    this.communicationAllowance = communicationAllowance;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getIdCustomerWebSafe() {
    return idCustomerWebSafe;
  }

  public void setIdCustomerWebSafe(String idCustomerWebSafe) {
    this.idCustomerWebSafe = idCustomerWebSafe;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getFirebaseTokenDevice() {
    return firebaseTokenDevice;
  }

  public void setFirebaseTokenDevice(String firebaseTokenDevice) {
    this.firebaseTokenDevice = firebaseTokenDevice;
  }

  public String getTokenFarmatodo() {
    return tokenFarmatodo;
  }

  public void setTokenFarmatodo(String tokenFarmatodo) {
    this.tokenFarmatodo = tokenFarmatodo;
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

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public Boolean isTermsAndConditions() {
    return termsAndConditions;
  }

  public void setTermsAndConditions(Boolean termsAndConditions) {
    this.termsAndConditions = termsAndConditions;
  }

  public Boolean isDataManagement() {
    return dataManagement;
  }

  public void setDataManagement(Boolean dataManagement) {
    this.dataManagement = dataManagement;
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

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public String getTokenFacebook() {
    return tokenFacebook;
  }

  public void setTokenFacebook(String tokenFacebook) {
    this.tokenFacebook = tokenFacebook;
  }

  public String getTokenGoogle() {
    return tokenGoogle;
  }

  public void setTokenGoogle(String tokenGoogle) {
    this.tokenGoogle = tokenGoogle;
  }

  public int getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(int idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
  }

  public String getRegisteredBy() {
    return registeredBy;
  }

  public void setRegisteredBy(String registeredBy) {
    this.registeredBy = registeredBy;
  }

  public String getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(String deliveryType) {
    this.deliveryType = deliveryType;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }


  public List<Interests> getInterests() {
    return interests;
  }

  public String getRegisteredByCall() {
    return registeredByCall;
  }

  public void setRegisteredByCall(String registeredByCall) {
    this.registeredByCall = registeredByCall;
  }

  public void setInterests(List<Interests> interests) {
    this.interests = interests;
  }

    public String getAnalyticsUUID() {
        return analyticsUUID;
    }

    public void setAnalyticsUUID(String analyticsUUID) {
        this.analyticsUUID = analyticsUUID;
    }

    public Integer getDocumentType() {
        return documentType;
    }

    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }

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

  public boolean hasValidAnalyticsUUID(){
    return analyticsUUID != null && !analyticsUUID.trim().isEmpty();
  }

  @Override
  public String toString() {
    return "Customer{" +
            "documentNumber='" + documentNumber + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", gender='" + gender + '\'' +
            ", countryId='" + countryId + '\'' +
            ", communicationAllowance=" + communicationAllowance +
            ", source='" + source + '\'' +
            ", password='" + password + '\'' +
            ", oldPassword='" + oldPassword + '\'' +
            ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
            ", deviceId='" + deviceId + '\'' +
            ", firebaseTokenDevice='" + firebaseTokenDevice + '\'' +
            ", tokenFarmatodo='" + tokenFarmatodo + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", city='" + city + '\'' +
            ", profileImageUrl='" + profileImageUrl + '\'' +
            ", termsAndConditions=" + termsAndConditions +
            ", dataManagement=" + dataManagement +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", addresses=" + addresses +
            ", tokenFacebook='" + tokenFacebook + '\'' +
            ", tokenGoogle='" + tokenGoogle + '\'' +
            ", idStoreGroup=" + idStoreGroup +
            ", registeredBy='" + registeredBy + '\'' +
            ", deliveryType='" + deliveryType + '\'' +
            ", cursor='" + cursor + '\'' +
            ", interests=" + interests +
            ", phoneNumbers=" + phoneNumbers +
            ", documentTypeId=" + documentTypeId +
            '}';
  }
}
