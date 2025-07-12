package com.imaginamos.farmatodo.model.order;

public class OrderActiveRequest {
    private Long idFarmatodo;
    private String documentNumber;
    private String email;

    public Long getIdFarmatodo() {
        return idFarmatodo;
    }

    public void setIdFarmatodo(Long idFarmatodo) {
        this.idFarmatodo = idFarmatodo;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
