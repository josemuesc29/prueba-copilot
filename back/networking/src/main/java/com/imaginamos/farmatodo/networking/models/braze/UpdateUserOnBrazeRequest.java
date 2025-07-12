package com.imaginamos.farmatodo.networking.models.braze;

import com.google.gson.Gson;

public class UpdateUserOnBrazeRequest {

    private String analyticsUUID;
    private String firstName;
    private String lastName;
    private String gender;
    private String countryId;
    private String phone;
    private String documentNumber;

    // Getters y Setters
    public String getAnalyticsUUID() {
        return analyticsUUID;
    }

    public void setAnalyticsUUID(String analyticsUUID) {
        this.analyticsUUID = analyticsUUID;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

        @Override
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }