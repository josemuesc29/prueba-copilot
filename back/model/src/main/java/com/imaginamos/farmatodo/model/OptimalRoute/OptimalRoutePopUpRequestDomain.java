package com.imaginamos.farmatodo.model.OptimalRoute;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class OptimalRoutePopUpRequestDomain {
    private String countryId;
    private Long orderId;
    private CustomerLocator customer;
    private ShoppingCartLocator shoppingCart;

    public OptimalRoutePopUpRequestDomain(
            String countryId, Long orderId, CustomerLocator customer,
            ShoppingCartLocator shoppingCart
    ) {
        this.countryId = countryId;
        this.orderId = orderId;
        this.customer = customer;
        this.shoppingCart = shoppingCart;
    }

    public OptimalRoutePopUpRequestDomain() {
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public CustomerLocator getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerLocator customer) {
        this.customer = customer;
    }

    public ShoppingCartLocator getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCartLocator shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
