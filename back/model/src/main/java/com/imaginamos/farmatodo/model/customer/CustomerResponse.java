package com.imaginamos.farmatodo.model.customer;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

public class CustomerResponse implements Serializable {
    private Integer id;
    private String countryId;
    private Integer documentTypeId;
    private String documentNumber;
    private String firstname;
    private String lastname;
    private String gender;
    private String phone;
    private String email;
    private String googleId;
    private String facebookId;
    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
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

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Customer toCustomer(){
        Customer customer = new Customer();
        customer.setId(this.id);
        customer.setCountryId(this.countryId);
        customer.setDocumentTypeId(this.documentTypeId);
        customer.setDocumentNumber(Objects.nonNull(this.documentNumber) ? this.documentNumber.toString() : "");
        customer.setFirstName(this.firstname);
        customer.setLastName(this.lastname);
        customer.setGender(this.gender);
        customer.setPhone(this.phone);
        customer.setEmail(this.email);
        customer.setTokenGoogle(this.googleId);
        customer.setTokenFacebook(this.facebookId);
        return customer;
    }

    public CustomerJSON toCustomerJson(){
        CustomerJSON customer = new CustomerJSON();
        customer.setId(this.id);
        customer.setCountry(this.countryId);
        customer.setDocumentType(Objects.nonNull(this.documentTypeId) ? this.documentTypeId.toString() : "");
        customer.setDocumentNumber(Objects.nonNull(this.documentNumber) ? new BigInteger(this.documentNumber) : BigInteger.ZERO);
        customer.setFirstName(this.firstname);
        customer.setLastName(this.lastname);
        customer.setGender(this.gender);
        customer.setPhone(this.phone);
        customer.setEmail(this.email);
        return customer;
    }

    @Override
    public String toString() {
        return "CustomerResponse{" +
                "id=" + id +
                ", countryId='" + countryId + '\'' +
                ", documentTypeId=" + documentTypeId +
                ", documentNumber=" + documentNumber +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", googleId='" + googleId + '\'' +
                ", facebookId='" + facebookId + '\'' +
                '}';
    }
}
