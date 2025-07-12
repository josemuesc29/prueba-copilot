package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;
import java.util.Map;

public class CustomerSessionRequest {
    private String profileId;
    private String state;
    private String email;
    private List<CartItem> cartItems;

    private Boolean dry;

    private LinkedTreeMap<String, Object> attributes;

    private List<String> couponCodes;

    private Long farmaCredits;

    private Map<String, Object> talonOneData;

    public Map<String, Object> getTalonOneData() {
        return talonOneData;
    }

    public void setTalonOneData(Map<String, Object> talonOneData) {
        this.talonOneData = talonOneData;
    }

    public Long getFarmaCredits() {
        return farmaCredits;
    }

    public void setFarmaCredits(Long farmaCredits) {
        this.farmaCredits = farmaCredits;
    }

    public List<String> getCouponCodes() {
        return couponCodes;
    }

    public void setCouponCodes(List<String> couponCodes) {
        this.couponCodes = couponCodes;
    }

    private Integer paymentCardId;

    public Integer getPaymentCardId() {
        return paymentCardId;
    }

    public void setPaymentCardId(Integer paymentCardId) {
        this.paymentCardId = paymentCardId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Boolean getDry() {
        return dry;
    }

    public void setDry(Boolean dry) {
        this.dry = dry;
    }

    public LinkedTreeMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(LinkedTreeMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "CustomerSessionRequest{" + "profileId=" + profileId + ", state=" + state + ", cartItems=" + cartItems + '}';
    }

    public boolean hasItems(){
        return this.getCartItems().size()>0;
    }
}
