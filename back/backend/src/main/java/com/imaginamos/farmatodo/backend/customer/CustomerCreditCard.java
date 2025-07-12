package com.imaginamos.farmatodo.backend.customer;

import com.imaginamos.farmatodo.model.customer.CreditCard;

import java.util.List;

public class CustomerCreditCard {
    private List<CreditCard> creditCardList;

    public CustomerCreditCard() {
    }

    public CustomerCreditCard(List<CreditCard> creditCardList) {
        this.creditCardList = creditCardList;
    }

    public List<CreditCard> getCreditCardList() {
        return creditCardList;
    }

    public void setCreditCardList(List<CreditCard> creditCardList) {
        this.creditCardList = creditCardList;
    }
}
