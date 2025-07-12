package com.imaginamos.farmatodo.model.order;


public class PaymentMethodRes {


    private  Long id;
    private  String description;
    private  Long positionIndex;
    private  Boolean creditCard;
    private  Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(Long positionIndex) {
        this.positionIndex = positionIndex;
    }

    public Boolean getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(Boolean creditCard) {
        this.creditCard = creditCard;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "PaymentMethodRes{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", positionIndex=" + positionIndex +
                ", creditCard=" + creditCard +
                ", active=" + active +
                '}';
    }
}
