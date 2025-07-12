package com.imaginamos.farmatodo.model.customer;

import java.util.Objects;

public class CustomerRequestKustomer {
    private String type;
    private String customerId;
    private String name;
    private String documentNumber;
    private String countryId;
    private String gender;
    private String phone;
    private String email;
    private String registeredBy;
    private String source;
    private boolean prime;
    private String creationDate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
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

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isPrime() {
        return prime;
    }

    public void setPrime(boolean prime) {
        this.prime = prime;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public static CustomerRequestKustomer buildFromCustomer(CustomerJSON customerJSON, Customer customer) {
        CustomerRequestKustomer customerRequestKustomer = new CustomerRequestKustomer();

        if (Objects.isNull(customerJSON) || Objects.isNull(customer)) {
            return customerRequestKustomer;
        }

        customerRequestKustomer.setName(customer.getFirstName() + " " + customer.getLastName());
        customerRequestKustomer.setDocumentNumber(customer.getDocumentNumber());
        customerRequestKustomer.setCountryId(customer.getCountryId());
        customerRequestKustomer.setGender(customer.getGender());
        customerRequestKustomer.setPhone(customer.getPhone());
        customerRequestKustomer.setEmail(customer.getEmail());
        customerRequestKustomer.setRegisteredBy(customer.getRegisteredBy());
        customerRequestKustomer.setSource(customer.getSource());
        customerRequestKustomer.setType("Client");
        customerRequestKustomer.setCustomerId(customerJSON.getId().toString());
        customerRequestKustomer.setPrime(false);
        customerRequestKustomer.setCreationDate(customerJSON.getCreationDate().toString());
        return customerRequestKustomer;
    }

    public static CustomerRequestKustomer buildFromCustomerToUpdate(CustomerJSON customerJSON) {
        CustomerRequestKustomer customerRequestKustomer = new CustomerRequestKustomer();

        if (Objects.isNull(customerJSON)) {
            return customerRequestKustomer;
        }

        customerRequestKustomer.setName(customerJSON.getFirstName() + " " + customerJSON.getLastName());
        customerRequestKustomer.setDocumentNumber(customerJSON.getDocumentNumber().toString());
        customerRequestKustomer.setCountryId(customerJSON.getCountry());
        customerRequestKustomer.setGender(customerJSON.getGender());
        customerRequestKustomer.setPhone(customerJSON.getPhone());
        customerRequestKustomer.setEmail(customerJSON.getEmail());
        return customerRequestKustomer;
    }
}
