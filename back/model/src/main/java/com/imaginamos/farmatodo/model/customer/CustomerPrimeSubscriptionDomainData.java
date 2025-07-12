package com.imaginamos.farmatodo.model.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class CustomerPrimeSubscriptionDomainData {
    private Boolean active;
    private BigInteger expirationDate;
    private BigInteger subscriptionDate;
    @JsonProperty("current_plan_sku")
    private BigInteger currentPlanSku;
    @JsonProperty("payment_method_used")
    private String paymentMethodUsed;
    @JsonProperty("payment_method_card_mask_used")
    private String paymentMethodCardMaskUsed;
    @JsonProperty("total_saved")
    private Double totalSaved;

    private Double savingCustomerNoPrime;

    private String primeId;

    private String franchise;

    public boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigInteger getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate != null ? BigInteger.valueOf(expirationDate) : null;
    }

    public BigInteger getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(Long subscriptionDate) {
        this.subscriptionDate = subscriptionDate != null ? BigInteger.valueOf(subscriptionDate) : null;
    }

    public BigInteger getCurrentPlanSku() {
        return currentPlanSku;
    }

    public void setCurrentPlanSku(Long currentPlanSku) {
        this.currentPlanSku = currentPlanSku != null ? BigInteger.valueOf(currentPlanSku) : null;
    }

    public String getPaymentMethodUsed() {
        return paymentMethodUsed;
    }

    public void setPaymentMethodUsed(String paymentMethodUsed) {
        this.paymentMethodUsed = paymentMethodUsed;
    }

    public String getPaymentMethodCardMaskUsed() {
        return paymentMethodCardMaskUsed;
    }

    public void setPaymentMethodCardMaskUsed(String paymentMethodCardMaskUsed) {
        this.paymentMethodCardMaskUsed = paymentMethodCardMaskUsed;
    }

    public Double getTotalSaved() {
        return totalSaved;
    }

    public void setTotalSaved(Double totalSaved) {
        this.totalSaved = totalSaved;
    }

    public Double getSavingCustomerNoPrime() {
        return savingCustomerNoPrime;
    }

    public void setSavingCustomerNoPrime(Double savingCustomerNoPrime) {
        this.savingCustomerNoPrime = savingCustomerNoPrime;
    }

    public String getPrimeId() {
        return primeId;
    }

    public void setPrimeId(String primeId) {
        this.primeId = primeId;
    }

    public String getFranchise() {
        return franchise;
    }

    public void setFranchise(String franchise) {
        this.franchise = franchise;
    }
}
