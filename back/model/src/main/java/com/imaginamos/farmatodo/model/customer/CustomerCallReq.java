package com.imaginamos.farmatodo.model.customer;

public class CustomerCallReq {

    private String phoneNumber;
    private Long documentNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Long documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Override
    public String toString() {
        return "CustomerCallReq{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", documentNumber=" + documentNumber +
                '}';
    }
}
