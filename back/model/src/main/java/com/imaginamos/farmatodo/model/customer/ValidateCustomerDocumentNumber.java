package com.imaginamos.farmatodo.model.customer;

public class ValidateCustomerDocumentNumber {
    private Long id;
    private Long documentNumber;
    private String email;
    private String registeredBy;

public ValidateCustomerDocumentNumber() {
    }

    public ValidateCustomerDocumentNumber(Long id, Long documentNumber, String email, String registeredBy) {
        this.id = id;
        this.documentNumber = documentNumber;
        this.email = email;
        this.registeredBy = registeredBy;
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDocumentNumber(Long documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    @Override
    public String toString() {
        return "ValidateCustomerDocumentNumber{" +
                "id=" + id +
                ", documentNumber=" + documentNumber +
                ", email='" + email + '\'' +
                ", registeredBy='" + registeredBy + '\'' +
                '}';
    }
}
