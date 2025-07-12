package com.imaginamos.farmatodo.model.customer;

import com.imaginamos.farmatodo.model.user.Token;

import java.util.List;

public class CustomerCallCenterJSON {
    private Long id;
    private String idCustomerWebSafe;
    private Token token;
    private Long documentNumber;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private List<PhoneNumber> phoneNumbers;
    private String analyticsUUID;

    public CustomerCallCenterJSON(CustomerCallResponseData customer, String idCustomerWebSafe, Token token) {
        this.id = customer.getId();
        this.idCustomerWebSafe = idCustomerWebSafe;
        this.token = token;
        this.documentNumber = customer.getDocumentNumber();
        this.firstname = customer.getFirstname();
        this.lastname = customer.getLastname();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
        this.phoneNumbers = customer.getPhoneNumbers();
    }
    /*
    public CustomerCallCenterJSON() {
    }

     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdCustomerWebSafe() {
        return idCustomerWebSafe;
    }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) {
        this.idCustomerWebSafe = idCustomerWebSafe;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Long documentNumber) {
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

    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }


    public String getAnalyticsUUID() {
        return analyticsUUID;
    }

    public void setAnalyticsUUID(String analyticsUUID) {
        this.analyticsUUID = analyticsUUID;
    }
}
