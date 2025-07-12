package com.imaginamos.farmatodo.model.customer;


public class CustomerFraudResponse {

    private Long idUser;
    private Long creditCardId;
    private Boolean isFraud;

    public CustomerFraudResponse(Long idUser, Boolean isFraud) {
        this.idUser = idUser;
        this.isFraud = isFraud;
    }

    public Long getIdUser() {
        return idUser;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Boolean getFraud() {
        return isFraud;
    }

    public void setFraud(Boolean fraud) {
        isFraud = fraud;
    }

    @Override
    public String toString() {
        return "CustomerFraudResponse{" +
                "idUser=" + idUser +
                ", creditCardId=" + creditCardId +
                ", isFraud=" + isFraud +
                '}';
    }
}
